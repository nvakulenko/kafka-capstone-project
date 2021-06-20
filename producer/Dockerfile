FROM python:3.8-slim-buster
COPY requirements.txt /opt/app/requirements.txt
WORKDIR /opt/app
COPY reddit_dataset.csv .
RUN pip install -r requirements.txt
ADD producer.py /
CMD [ "python" ,"/producer.py"]
ENV PYTHONUNBUFFERED 1
EXPOSE 8050