#!/usr/bin/env python3
"""Compare recorded releases and generate complete source/JVM signature appendices."""

import difflib
import hashlib
import json
import re
from pathlib import Path
from zipfile import ZipFile


def sections(text):
    result = {}
    for block in text.split('Compiled from ')[1:]:
        name = re.search(r"\b(?:class|interface) ([\w.$]+)", block).group(1)
        result[name] = 'Compiled from ' + block
    return result


def methods(block):
    result = set()
    declaration = None
    for line in block.splitlines():
        if re.match(r"\s+(public|protected) .*\(", line):
            declaration = line.split("(", 1)[0].split()[-1]
        elif declaration and "descriptor:" in line:
            result.add((declaration, line.split("descriptor:", 1)[1].strip()))
            declaration = None
    return result


def main():
    root = Path(__file__).resolve().parents[1]
    old, new = (root / "artifacts" / version for version in ("1.0", "1.1"))
    for release in (old, new):
        for line in (release / "SHA256SUMS").read_text().splitlines():
            digest, name = line.split("  ", 1)
            assert hashlib.sha256((release / name).read_bytes()).hexdigest() == digest, name
    for path in (old / "maven/styling/1.0").iterdir():
        assert path.read_bytes() == (new / "maven/styling/1.0" / path.name).read_bytes(), path
    assert (old / "bytecode/styling/classes.jar").read_bytes() == (new / "bytecode/styling/classes.jar").read_bytes()
    assert (old / "bytecode/styling/instructions.txt").read_bytes() == (new / "bytecode/styling/instructions.txt").read_bytes()
    selected = [
        "v1.Contract1", "v1.Contract1$DefaultImpls", "v1.Contract1Wrapper", "v1.Contract1Kt",
        "v2.Contract2", "v2.Contract2$DefaultImpls", "v2.Contract2$Contract", "v2.Contract2Kt",
        "v2.Contract2Impl", "VersionedFunctionsKt",
    ]
    names = ["com.alexcawl.contract.playground.component." + name for name in selected]
    dumps = [sections((release / "bytecode/component/signatures.txt").read_text()) for release in (old, new)]
    comparison = {}
    for name in names:
        before, after = (methods(dump[name]) for dump in dumps)
        removed = sorted(before - after)
        comparison[name] = {"old_methods": len(before), "new_methods": len(after), "removed": removed, "added": sorted(after - before)}
        if name.endswith("VersionedFunctionsKt"):
            assert removed == [("withJvmOverloads$default", "(Ljava/lang/String;Ljava/lang/String;ILjava/lang/Object;)Ljava/lang/String;")]
        elif not name.endswith("Contract2Impl"):
            assert not removed, (name, removed)
    differences = root / "artifacts/diffs"
    differences.mkdir(parents=True, exist_ok=True)
    (differences / "public-methods.json").write_text(json.dumps(comparison, indent=2) + "\n")
    for filename in ("signatures.txt", "instructions.txt"):
        before, after = ((release / "bytecode/component" / filename).read_text().splitlines(keepends=True) for release in (old, new))
        diff = difflib.unified_diff(before, after, fromfile="component-1.0/" + filename, tofile="component-1.1/" + filename)
        (differences / filename.replace(".txt", ".diff")).write_text("".join(diff))
    documentation = root / "docs"
    documentation.mkdir(exist_ok=True)
    appendix = ["# Полные сигнатуры Component 1.0 и 1.1\n\n",
                "Исходники ниже извлечены из оригинальных опубликованных sources.jar. JVM-сигнатуры взяты из сохранённого javap -p -s. Типы, имена JVM-методов и descriptors не сокращены.\n\n"]
    for version, release, dump in zip(("1.0", "1.1"), (old, new), dumps):
        appendix.append(f"## Component {version}: исходники\n\n")
        with ZipFile(release / f"maven/component/{version}/component-{version}-sources.jar") as sources:
            for basename in ("Contract1.kt", "Contract2.kt", "VersionedFunctions.kt"):
                path = next(path for path in sources.namelist() if path.endswith("/" + basename) or path == basename)
                appendix.append(f"### {basename}\n\n```kotlin\n{sources.read(path).decode()}```\n\n")
        appendix.append(f"## Component {version}: JVM\n\n")
        for name in names:
            appendix.append(f"### {name}\n\n```text\n{dump[name].rstrip()}\n```\n\n")
    (documentation / "API_SIGNATURES.md").write_text("".join(appendix))
    print("Verified checksums, unchanged Styling bytes, and all retained public contract method descriptors.")
    print("Only the plain @JvmOverloads example loses an old public method: withJvmOverloads$default.")


if __name__ == "__main__":
    main()
