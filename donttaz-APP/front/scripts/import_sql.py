import subprocess, pathlib

sql_file = pathlib.Path(__file__).parent / "transactions.sql"
cmd = ["mysql", "-u", "root", "-pa1d2m3i4n5", "-h", "127.0.0.1", "-P", "3333", "test_cji"]

with open(sql_file, encoding="utf-8") as f:
    result = subprocess.run(cmd, stdin=f)

print("완료" if result.returncode == 0 else f"실패: {result.returncode}")
