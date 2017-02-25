.PHONY: install
install:
	test -d ../image && cd ../work/tribaltrouble-master/tt/build/dist/common && tar cf - . | (cd ../../../../../../image && tar xf -)
