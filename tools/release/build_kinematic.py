#!/usr/bin/env python3
"""Package an installable kinematic variant from the exact recovered 26.3 checkpoint."""
import argparse
import hashlib
import json
import os
from pathlib import Path
import shutil
import subprocess
import tempfile
import zipfile

BASE_SHA256 = 'c9ad3657776510f2d5b02131b96ccb3b189fb6ec03f28604b2abbb9726de02a7'
OSTINATO_SHA256 = 'fd4059becbf8a93bf39b78108b2a795d5b8cf33a01f73a1a964d1318ec7c0d52'
ENTRYPOINT = 'adris.altoclef.release.KinematicRelease'
NAME = 'altoclef-26.3-0.22.2-kinematic.jar'

def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()

def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--base', type=Path, required=True)
    parser.add_argument('--ostinato', type=Path, required=True)
    parser.add_argument('--fabric-loader', type=Path, required=True)
    parser.add_argument('--output', type=Path, required=True)
    parser.add_argument('--javac', default='javac', help='JDK 25 javac executable')
    args = parser.parse_args()
    if digest(args.base) != BASE_SHA256 or digest(args.ostinato) != OSTINATO_SHA256:
        parser.error('Expected the original 26.3 checkpoint TenorClef and Ostinato jars; checksum mismatch')
    args.output.mkdir(parents=True, exist_ok=True)
    source = Path(__file__).parent / 'kinematic/KinematicRelease.java'
    with tempfile.TemporaryDirectory(prefix='tenorclef-kinematic-') as temp:
        classes = Path(temp) / 'classes'
        subprocess.run([args.javac, '--release', '25', '-encoding', 'UTF-8', '-d', str(classes),
                        '-classpath', os.pathsep.join(map(str, [args.ostinato.resolve(),
                                                             args.fabric_loader.resolve()])),
                        str(source)], check=True)
        with zipfile.ZipFile(args.base) as base:
            entries = {name: base.read(name) for name in base.namelist() if not name.endswith('/')}
        metadata = json.loads(entries['fabric.mod.json'])
        assert metadata['id'] == 'altoclef'
        assert metadata['entrypoints']['main'] == ['adris.altoclef.AltoClef']
        metadata['version'] = '26.3-0.22.2-kinematic'
        metadata['name'] = 'TenorClef (Kinematic Experimental)'
        metadata['description'] = 'TenorClef 26.3 with experimental kinematic travel enabled at startup.'
        metadata['contact']['homepage'] = 'https://github.com/nicohza/gogoclef'
        metadata['contact']['sources'] = 'https://github.com/nicohza/gogoclef'
        metadata['entrypoints']['main'].insert(0, ENTRYPOINT)
        entries['fabric.mod.json'] = (json.dumps(metadata, indent=2) + '\n').encode()
        for path in classes.rglob('*.class'):
            entries[path.relative_to(classes).as_posix()] = path.read_bytes()
        entries['META-INF/tenorclef-kinematic-source.java'] = source.read_bytes()
        entries['META-INF/tenorclef-kinematic-base.sha256'] = (BASE_SHA256 + '\n').encode()
        target = args.output / NAME
        with zipfile.ZipFile(target, 'w', compression=zipfile.ZIP_DEFLATED, compresslevel=9) as jar:
            for name, data in sorted(entries.items()):
                info = zipfile.ZipInfo(name, (2026, 10, 4, 0, 0, 0))
                info.compress_type = zipfile.ZIP_DEFLATED
                info.external_attr = 0o644 << 16
                jar.writestr(info, data)
        with zipfile.ZipFile(target) as jar:
            assert jar.testzip() is None
            with zipfile.ZipFile(args.base) as base:
                for name in base.namelist():
                    if not name.endswith('/') and name != 'fabric.mod.json':
                        assert jar.read(name) == base.read(name), name
        shutil.copy2(args.ostinato, args.output / args.ostinato.name)
        print(f'PASS: {target}; original runtime entries preserved; SHA256 {digest(target)}')

if __name__ == '__main__':
    main()
