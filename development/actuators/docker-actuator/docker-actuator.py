from flask import Flask
from flask import request
import json
import os
import logging
import logging.config
import subprocess
import docker
from communication import Communication
from data import Data
from message import Message
from message import ComplexEncoder
from observation import Observation
from HandleRequest import HandleRequest
# from tmalibrary.actuator import *

demoActuator = Flask(__name__)

logger = logging.getLogger(__name__)
logger.info('Starting Docker Actuator')


@demoActuator.route('/docker/act', methods=['POST'])
def process_message():
    # load json file
    input = request.get_data()
    message = HandleRequest()
    payload = message.processRequest(input)
    operation = executeaction(payload.action)
    return message.generateResponse(str(operation))


def run(cmd):
    res = subprocess.check_output(cmd, shell=True)
    return res


def get_current_memory_limit(container_name):
    data = run(f"docker stats {container_name} --no-stream")
    mem_limit = None
    lines = data.decode('UTF-8').split("\n")
    for line in lines[1:]:
        bits = line.split("  ")
        bits = [bit for bit in bits if bit.strip() != ""]
        if len(bits) == 8:
            mem_limit = bits[3].split("/")[1].strip().replace("MiB", "")
    return mem_limit


# def get_current_cpu_limit(container_name):
#     data = run(f"docker container inspect {container_name} | grep -i cpu")
#     cpu_limit = data.get("CpuCount")
#     return cpu_limit

# Execute the action
def executeaction(action):
    mem_limit = int(get_current_memory_limit("priva")) + 100
    # cpu_limit = int(get_current_cpu_limit("priva")) + 1
    cpu_limit = "0-1"

    logger.info('Action: %s', action)
    switcher = {
        "INCREASE_MEM": f"mem_limit={mem_limit}",
        "INCREASE_CPU": f"cpuset_cpus={cpu_limit}"
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


# load logging configuration file
def setup_logging(default_path='logging.json', env_key='LOG_CFG'):
    path = default_path
    value = os.getenv(env_key, None)
    if value:
        path = value
    if os.path.exists(path):
        with open(path, 'rt') as f:
            config = json.load(f)
        logging.config.dictConfig(config)
    else:
        logging.basicConfig(level=logging.DEBUG)


if __name__ == '__main__':
    setup_logging()
    logger = logging.getLogger(__name__)
    logger.info('Initializing Docker Actuator')
    demoActuator.run(debug='True', host='0.0.0.0', port=8000)

    # # connect to docker
    # cli = docker.from_env()
    # # get container
    # container = cli.containers.get("priva")
    #
    # container.update(cpu_count="1")
