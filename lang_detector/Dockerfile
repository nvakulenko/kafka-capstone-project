FROM python:3.9-slim-buster
COPY requirements.txt /opt/app/requirements.txt
WORKDIR /opt/app
RUN pip install -r requirements.txt
ADD lang_detect_test.py /
CMD [ "python" ,"/lang_detect_test.py"]
ENV PYTHONUNBUFFERED 1
EXPOSE 8050