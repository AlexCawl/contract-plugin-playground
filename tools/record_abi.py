#!/usr/bin/env python3
"""Publish the selected Component, keep Styling 1.0 frozen, and preserve original ABI evidence."""

import argparse
import hashlib
import json
import shutil
import subprocess
import xml.etree.ElementTree as ET
from pathlib import Path
from zipfile import ZipFile


def sha256(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def test_results(directory):
    results = []
    for path in sorted(directory.glob("TEST-*.xml")):
        for case in ET.parse(path).getroot().findall("testcase"):
            failure = case.find("failure")
            results.append({
                "class": case.get("classname"),
                "name": case.get("name"),
                "status": "failed" if failure is not None else
                          "skipped" if case.find("skipped") is not None else "passed",
                "failure_type": failure.get("type") if failure is not None else None,
            })
    return results


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("version", choices=("1.0", "1.1"))
    parser.add_argument("--publish-styling", action="store_true")
    args = parser.parse_args()
    assert args.publish_styling == (args.version == "1.0"), "Only the 1.0 stage publishes Styling"
    root = Path(__file__).resolve().parents[1]
    output = root / "artifacts" / args.version
    output.mkdir(parents=True, exist_ok=True)
    logs = output / "logs"
    logs.mkdir(exist_ok=True)
    commands = []

    def run(name, command):
        log = logs / name
        if log.exists():
            archive = logs / "previous-attempts"
            archive.mkdir(exist_ok=True)
            index = len(list(archive.iterdir())) + 1
            shutil.copy2(log, archive / f"{index}-{name}")
        print("Running:", " ".join(command), flush=True)
        with log.open("wb") as stream:
            result = subprocess.run(command, cwd=root, stdout=stream, stderr=subprocess.STDOUT)
        commands.append({"command": command, "exit_code": result.returncode, "log": str(log.relative_to(output))})
        print(f"Exit {result.returncode}: {log.relative_to(root)}", flush=True)
        return result.returncode

    def gradle(name, *tasks):
        return run(name, [str(root / "gradlew"), *tasks, "--max-workers=1", "--console=plain"])

    maven = Path.home() / ".m2/repository/com/alexcawl/contract/playground"
    styling_aar = maven / "styling/1.0/styling-1.0.aar"
    if args.version == "1.1":
        frozen = root / "artifacts/1.0/maven/styling/1.0/styling-1.0.aar"
        assert sha256(styling_aar) == sha256(frozen), "Styling 1.0 changed before the upgrade"

    assert gradle("environment.log", "--version") == 0
    assert gradle("publish-component.log", ":component:publishReleasePublicationToMavenLocal", ":component:lintDebug") == 0
    if args.publish_styling:
        assert gradle("publish-styling.log", ":styling:publishReleasePublicationToMavenLocal", ":styling:lintDebug") == 0
    frozen_hash = sha256(styling_aar)
    assert gradle("dependencies.log", ":app:dependencies", "--configuration", "debugRuntimeClasspath") == 0
    assert gradle("component-resolution.log", ":app:dependencyInsight", "--dependency", "com.alexcawl.contract.playground:component", "--configuration", "debugRuntimeClasspath") == 0
    app_exit = gradle("app-verification.log", ":app:testDebugUnitTest", ":app:assembleDebug", ":app:lintDebug", "--continue")
    component_exit = gradle("contract-semantics.log", ":component:testDebugUnitTest", "--continue")

    app_tests = test_results(root / "app/build/test-results/testDebugUnitTest")
    failures = [case for case in app_tests if case["status"] == "failed"]
    assert len(app_tests) == 18, app_tests
    expected_failures = [] if args.version == "1.0" else ["jvmOverloadsDefault"]
    assert [case["name"] for case in failures] == expected_failures, failures
    if failures:
        assert failures[0]["failure_type"] == "java.lang.NoSuchMethodError", failures
    assert app_exit == (0 if args.version == "1.0" else 1), app_exit
    assert sum(case["status"] == "skipped" for case in app_tests) == (1 if args.version == "1.0" else 0)
    semantics = test_results(root / "component/build/test-results/testDebugUnitTest")
    assert len(semantics) == 4, semantics
    assert {(case["class"].rsplit(".", 1)[-1], case["name"]) for case in semantics if case["status"] == "failed"} == {
        ("Contract1Test", "isRebinding"), ("Contract2Test", "isDelegated"),
    }
    assert component_exit == 1
    assert sha256(styling_aar) == frozen_hash, "Styling was modified during verification"
    dependencies = (logs / "dependencies.log").read_text()
    assert "project :component" not in dependencies and "project :styling" not in dependencies
    assert f"com.alexcawl.contract.playground:component:{args.version}" in dependencies
    assert "com.alexcawl.contract.playground:styling:1.0" in dependencies

    for module, version in (("component", args.version), ("styling", "1.0")):
        destination = output / "maven" / module / version
        destination.mkdir(parents=True, exist_ok=True)
        for path in sorted((maven / module / version).glob(f"{module}-{version}*")):
            if path.is_file():
                shutil.copy2(path, destination / path.name)
        bytecode = output / "bytecode" / module
        bytecode.mkdir(parents=True, exist_ok=True)
        with ZipFile(destination / f"{module}-{version}.aar") as aar:
            jar = bytecode / "classes.jar"
            jar.write_bytes(aar.read("classes.jar"))
        with ZipFile(jar) as archive:
            classes = sorted(name[:-6].replace("/", ".") for name in archive.namelist() if name.endswith(".class"))
        (bytecode / "classes.txt").write_text("\n".join(classes) + "\n")
        for name, flags in (("signatures.txt", ["-p", "-s"]), ("instructions.txt", ["-p", "-s", "-c"]), ("verbose.txt", ["-p", "-v"])):
            with (bytecode / name).open("wb") as stream:
                subprocess.run(["javap", "-classpath", str(jar), *flags, *classes], cwd=root, stdout=stream, stderr=subprocess.STDOUT, check=True)
        report = output / "reports" / module
        report.mkdir(parents=True, exist_ok=True)
        shutil.copytree(root / module / "build/reports", report, dirs_exist_ok=True)
    shutil.copytree(root / "app/build/reports", output / "reports/app", dirs_exist_ok=True)
    for module in ("component", "app"):
        shutil.copytree(root / module / "build/test-results/testDebugUnitTest", output / "test-results" / module, dirs_exist_ok=True)
    for module in ("component", "app"):
        lint = ET.parse(root / module / "build/reports/lint-results-debug.xml")
        assert all(issue.get("severity") not in ("Error", "Fatal") for issue in lint.findall("issue"))
    shutil.copy2(root / "app/build/outputs/apk/debug/app-debug.apk", output / "app-debug.apk")
    config = output / "build-config"
    for name in ("settings.gradle.kts", "build.gradle.kts", "gradle.properties", "gradle/libs.versions.toml", "gradle/gradle-daemon-jvm.properties", "component/build.gradle.kts", "styling/build.gradle.kts", "app/build.gradle.kts"):
        path = config / name
        path.parent.mkdir(parents=True, exist_ok=True)
        shutil.copy2(root / name, path)
    manifest = {
        "component": f"com.alexcawl.contract.playground:component:{args.version}",
        "styling": "com.alexcawl.contract.playground:styling:1.0",
        "styling_aar_sha256": frozen_hash,
        "styling_republished": args.publish_styling,
        "source_commit": subprocess.check_output(["git", "rev-parse", "HEAD"], cwd=root, text=True).strip(),
        "commands": commands,
        "app_tests": app_tests,
        "contract_semantics_tests": semantics,
    }
    (output / "manifest.json").write_text(json.dumps(manifest, indent=2) + "\n")
    hashes = [f"{sha256(path)}  {path.relative_to(output)}" for path in sorted(output.rglob("*")) if path.is_file() and path.name != "SHA256SUMS"]
    (output / "SHA256SUMS").write_text("\n".join(hashes) + "\n")
    print(f"Recorded {len(app_tests)} binary checks; expected failures: {expected_failures}", flush=True)


if __name__ == "__main__":
    main()
