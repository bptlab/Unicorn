FROM python:3.8-rc-alpine
RUN pip install --no-cache-dir mysql-connector-python
COPY ["./scripts_common/*", "./"]
# Run configuration script when image is loaded
CMD ["python", "dbadmin.py" ]