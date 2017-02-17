scala_library(
  dependencies=[
    'src/jvm/fr/cnrs/liris/privamov/core/model',
    'src/jvm/fr/cnrs/liris/privamov/core/model:json',
    'src/jvm/fr/cnrs/liris/privamov/core/lppm',
    'src/jvm/fr/cnrs/liris/common/geo',
    'src/jvm/fr/cnrs/liris/common/geo:json',
    'src/jvm/com/twitter/querulous',
    '3rdparty/jvm/com/twitter/finatra:http',
    '3rdparty/jvm/com/twitter/finatra:httpclient',
    scoped('3rdparty/jvm/org/postgresql', scope='runtime'),
  ],
  resources=[
    'src/node/fr/cnrs/liris/privamov/spotme:assets',
  ],
  excludes=[
    exclude('commons-logging', 'commons-logging'),
  ],
  sources=rglobs('*.scala')
)

jvm_binary(
  name='bin',
  dependencies=[
    ':spotme',
    '3rdparty/jvm:logback',
  ],
  resources=[
    'src/resources/fr/cnrs/liris/privamov/svc:logback',
  ],
  main='fr.cnrs.liris.privamov.spotme.SpotmeServerMain',
  basename='privamov-spotme',
)
