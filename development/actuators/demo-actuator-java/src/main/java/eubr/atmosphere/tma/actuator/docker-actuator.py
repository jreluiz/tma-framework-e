import subprocess
import docker
import sys


def run(cmd):
    res = subprocess.check_output(cmd, shell=True)
    return res


def get_current_memory_limit(container_name):
    data = run("docker stats %s --no-stream".format(container_name))
    mem_limit = None
    lines = data.decode('UTF-8').split("\n")
    for line in lines[1:]:
        bits = line.split("  ")
        bits = [bit for bit in bits if bit.strip() != ""]
        if len(bits) == 8:
            mem_limit = bits[3].split("/")[1].strip().replace("MiB", "")
    return mem_limit


# Execute the action
def execute_action(action):
    mem_limit = int(get_current_memory_limit("priva")) + 100
    # cpu_limit = int(get_current_cpu_limit("priva")) + 1
    cpu_limit = "0-1"

    logger.info('Action: %s', action)
    switcher = {
        "INCREASE_MEM": "mem_limit=%s".format(mem_limit),
        "INCREASE_CPU": "cpuset_cpus=%s".format(cpu_limit)
        # "DECREASE_MEM"
        # "DECREASE_CPU"
    }
    action_exec = switcher.get(action, "Not defined action: " + action)

    # connect to docker
    cli = docker.from_env()
    # get container
    container = cli.containers.get("priva")

    container.update(action_exec)

    return action_executed


# if __name__ == '__main__':
#    executeaction(sys.argv[1] + '')
