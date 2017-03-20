.PHONY: spotme

spotme:
	cd src/node/fr/cnrs/liris/privamov/spotme && yarn install && npm run build
	./pants binary src/jvm/fr/cnrs/liris/privamov/spotme:bin
