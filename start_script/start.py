import subprocess
import os
from pathlib import Path

docker_compose_path = Path(__file__).resolve().parent.parent

def verificar_docker_instalado():
    try:
        resultado = subprocess.run(["docker", "--version"], check=True, capture_output=True, text=True)
        print("✅ Docker está instalado:")
        print(resultado.stdout.strip())
        return True
    except FileNotFoundError:
        print("❌ Docker não está instalado ou não está no PATH.")
        return False
    except subprocess.CalledProcessError as e:
        print("⚠️ Docker está instalado, mas houve um erro ao executar:", e)
        return False

def start_docker_compose():
    compose_file = docker_compose_path / "docker-compose.yml"
    if not compose_file.exists():
        print(f"❌ Arquivo docker-compose.yml não encontrado em {docker_compose_path}")
        return

    os.chdir(docker_compose_path)
    print(f"🚀 Subindo containers com docker-compose em: {docker_compose_path}")
    subprocess.run(["docker-compose", "up", "--build"])

if __name__ == "__main__":
    if verificar_docker_instalado():
        start_docker_compose()
