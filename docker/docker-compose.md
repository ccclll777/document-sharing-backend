


docker-compose -f docker-compose-m1.yml up -d

docker cp docker/esplugins/elasticsearch-analysis-ik-7.16.2 elasticsearch:/usr/share/elasticsearch/plugins
进入容器内 /usr/share/elasticsearch/plugins 执行
./elasticsearch-plugin list
