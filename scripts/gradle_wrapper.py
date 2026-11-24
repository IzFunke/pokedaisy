"""Run this checkout's Gradle wrapper using the host platform's launcher."""

import os
import subprocess
from pathlib import Path


def run_gradle(root, args, **kwargs):
    root = Path(root).resolve()
    wrapper = root / ("gradlew.bat" if os.name == "nt" else "gradlew")
    if not wrapper.is_file():
        raise FileNotFoundError(f"Gradle wrapper not found: {wrapper}")

    command = [str(wrapper), *(str(arg) for arg in args)]
    kwargs.setdefault("cwd", root)
    if os.name == "nt":
        # Windows must hand a .bat file to cmd.exe; launching the extensionless
        # POSIX `gradlew` can open the Windows app picker.
        command[0] = ".\\gradlew.bat"
        kwargs["shell"] = True
        command = subprocess.list2cmdline(command)
    return subprocess.run(command, **kwargs)
