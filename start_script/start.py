import subprocess
import os

# Caminho onde está o docker-compose.yml
docker_compose_path = "../"  # ou o caminho absoluto se preferir

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
    os.chdir(docker_compose_path)
    subprocess.run(["docker-compose", "up", "--build"])

if __name__ == "__main__":
    verificar_docker_instalado()
    start_docker_compose()
