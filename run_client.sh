java -Djava.rmi.server.codebase=file:/tmp/common.jar -Djava.rmi.server.hostname=192.168.0.6 -Djava.security.policy=dummy.policy -Dhost=192.168.0.6 -Dport=1100 -DobjectName=ttt -classpath /home/wojtek/Dropbox/home/wojtek/Documents/EAIE_Informatyka/06_sem/rozpro/2-zad/tttrmi/jars/common.jar:/home/wojtek/Dropbox/home/wojtek/Documents/EAIE_Informatyka/06_sem/rozpro/2-zad/tttrmi/build/classes/server:/home/wojtek/Dropbox/home/wojtek/Documents/EAIE_Informatyka/06_sem/rozpro/2-zad/tttrmi/build/classes/client -Dnick=jan client.Client