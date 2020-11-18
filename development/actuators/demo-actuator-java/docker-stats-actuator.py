import subprocess
import docker
import sys


def run(cmd):
    res = subprocess.check_output(cmd, shell=True)
    return res


def get_current_memory_limit(container_name):
    data = run("docker stats {} --no-stream".format(container_name))
    mem_limit = None
    lines = data.decode('UTF-8').split("\n")
    for line in lines[1:]:
        bits = line.split("  ")
        bits = [bit for bit in bits if bit.strip() != ""]
        if len(bits) == 8:
            mem_limit = bits[3].split("/")[1].strip().replace("MiB", "")
    return mem_limit


def get_container(container_name):
    try:
        # connect to docker
        cli = docker.from_env()
        # get container
        container = cli.containers.get("priva")
        return container
    except Exception as e:
        print("---Erro ao conectar ao container {} - {}".format(container_name, str(e)))
        return None


# Execute the action
def execute_action(action):
    print("Action received: {}".format(action))

    try:
        container = get_container("priva")
        if container:
            if action == "increase_mem":
                print("Executing memory increase")
                mem_limit = int(get_current_memory_limit("priva")) + 100
                if mem_limit > 2500:
                    print("Memoria maxima atingida!")
                else:
                    container.update(mem_limit="{}m".format(mem_limit))
            elif action == "increase_cpu":
                print("Executing cpu increase")
                container.update(cpuset_cpus="0-1")
            elif action == "decrease_mem":
                print("Executing memory decrease")
                mem_limit = int(get_current_memory_limit("priva")) - 100
                if mem_limit < 100:
                    print("Memoria minima atingida!")
                else:
                    container.update(mem_limit="{}m".format(mem_limit))
            elif action == "decrease_cpu":
                print("Executing cpu decrease")
                container.update(cpuset_cpus="0")
        else:
            print('---Erro ao conectar ao container')
            return False
    except Exception as e:
        print("Error {}".format(str(e)))
        return False

    return True

