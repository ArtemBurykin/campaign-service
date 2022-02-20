### About
The service is to run a mailing campaign. It gathers all the events
for campaigns. And allows you to control the process of mailing.
### Testing
In order to launch tests you need to run:
```shell
make up-test 
```
And then launch tests in the IDE.

### Developing
In order to launch the dev server you need to run
```shell
docker-compose -f docker-compose.dev.yml up --build
```

### Production
```shell
docker-compose up --build
```