.PHONY: install
install:
	mkdir -p /usr/share/games/tribaltrouble
	test -d /usr/share/games/tribaltrouble && cd ../work/tribaltrouble-master/tt/build/dist/common/ && tar cf - . | (cd /usr/share/games/tribaltrouble; tar xf -)
