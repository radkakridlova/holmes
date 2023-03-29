from flask import Flask, jsonify, request, send_file, redirect
from tempfile import NamedTemporaryFile

app = Flask(__name__, static_folder='frontend/build/static')


@app.route("/progress/<task_uid>", methods=['GET'])
def progress(task_uid):
    return jsonify(status=task_uid)


@app.route("/upload", methods=['POST'])
def upload():
    with NamedTemporaryFile() as f:
        request.files['file'].save(f.name)
    return redirect("/progress/20")


@app.route("/list")
def list():
    return jsonify([{"id": "a", "time": "b"} for x in range(3)])


@app.route("/logs/<task_uid>/<log_type>")
def logs(task_uid, log_type):
    #with open("C:\\Users\\h\\Desktop\\drakvuf outputs\\drakvuf-output2\\output\\regmon.log", "r") as f:
    with open("/var/lib/drakcore/minio/drakrun/d753c33c-7dfa-40af-90d3-7ea43ccd7e1d/regmon.log", "r") as f:
        return send_file(f.name, mimetype='text/plain')


@app.route("/status/<task_uid>")
def status(task_uid):
    return jsonify({"status": "done"})


def main():
    listen_host = "0.0.0.0"
    listen_port = 5000
    app.run(host=listen_host, port=listen_port)


if __name__ == "__main__":
    main()
