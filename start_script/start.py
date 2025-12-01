#!/usr/bin/env python3
"""
Script para iniciar a aplicação Digital Signature usando Docker Compose.
Verifica se o Docker está instalado e inicia os containers automaticamente.
"""

import subprocess
import sys
import os
from pathlib import Path

# Caminho do diretório onde está o docker-compose.yml
docker_compose_path = Path(__file__).resolve().parent.parent

def verificar_docker_instalado():
    """Verifica se o Docker está instalado e acessível."""
    try:
        resultado = subprocess.run(
            ["docker", "--version"], 
            check=True, 
            capture_output=True, 
            text=True
        )
        print("✓ Docker está instalado:")
        print(f"  {resultado.stdout.strip()}")
        return True
    except FileNotFoundError:
        print("✗ Docker não está instalado ou não está no PATH.")
        print("  Por favor, instale o Docker em: https://www.docker.com/get-started")
        return False
    except subprocess.CalledProcessError as e:
        print(f"✗ Erro ao verificar Docker: {e}")
        return False

def verificar_docker_compose():
    """Verifica se o Docker Compose está disponível."""
    try:
        # Tenta docker compose (versão nova)
        resultado = subprocess.run(
            ["docker", "compose", "version"], 
            check=True, 
            capture_output=True, 
            text=True
        )
        print("✓ Docker Compose está instalado:")
        print(f"  {resultado.stdout.strip()}")
        return "docker compose"
    except (FileNotFoundError, subprocess.CalledProcessError):
        # Tenta docker-compose (versão antiga)
        try:
            resultado = subprocess.run(
                ["docker-compose", "--version"], 
                check=True, 
                capture_output=True, 
                text=True
            )
            print("✓ Docker Compose está instalado:")
            print(f"  {resultado.stdout.strip()}")
            return "docker-compose"
        except (FileNotFoundError, subprocess.CalledProcessError):
            print("✗ Docker Compose não está instalado.")
            print("  Por favor, instale o Docker Compose.")
            return None

def start_docker_compose(compose_command):
    """Inicia os containers usando Docker Compose."""
    compose_file = docker_compose_path / "docker-compose.yml"
    
    if not compose_file.exists():
        print(f"✗ Arquivo docker-compose.yml não encontrado em {docker_compose_path}")
        return False

    os.chdir(docker_compose_path)
    print(f"\n{'='*60}")
    print(f"Iniciando containers em: {docker_compose_path}")
    print(f"{'='*60}\n")
    
    try:
        if compose_command == "docker compose":
            subprocess.run(["docker", "compose", "up", "--build"], check=True)
        else:
            subprocess.run(["docker-compose", "up", "--build"], check=True)
        return True
    except subprocess.CalledProcessError as e:
        print(f"\n✗ Erro ao iniciar containers: {e}")
        return False
    except KeyboardInterrupt:
        print("\n\n✓ Aplicação interrompida pelo usuário.")
        print("Para parar os containers, execute:")
        if compose_command == "docker compose":
            print("  docker compose down")
        else:
            print("  docker-compose down")
        return True

def main():
    """Função principal."""
    print("\n" + "="*60)
    print("Digital Signature - Iniciador Automático")
    print("="*60 + "\n")
    
    # Verifica Docker
    if not verificar_docker_instalado():
        sys.exit(1)
    
    # Verifica Docker Compose
    compose_command = verificar_docker_compose()
    if not compose_command:
        sys.exit(1)
    
    print("\n" + "-"*60)
    print("Tudo pronto! Iniciando aplicação...")
    print("-"*60 + "\n")
    
    # Inicia os containers
    if start_docker_compose(compose_command):
        print("\n✓ Aplicação finalizada com sucesso!")
    else:
        print("\n✗ Houve um erro ao executar a aplicação.")
        sys.exit(1)

if __name__ == "__main__":
    main()
