https://kafka.apache.org/quickstart

https://raw.githubusercontent.com/nevostruev/csv2json/master/csv2json.pl


Commandline:

perl csv2json.pl countries2.csv  | ./bin/windows/kafka-console-producer.bat --broker-list localhost:9092 --topic  countries