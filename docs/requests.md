### To create a campaign
```shell
curl -X POST -H "Content-Type: application/json" \
    -d '{"title": "a new campaign", "templateId": "faedb60a-6502-4aa8-a848-bfad3c8e2715", "templateConfig": {"date": "2017-02-02"}, "recipients": {"faedb60a-6502-4aa8-a848-bfad3c8e2711": {"firstname":"artem"}}}' \
    http://localhost:90/campaigns
```

```shell
curl -X PUT -H "Content-Type: application/json" http://localhost:90/campaigns/829d85ba-99bc-44dd-b81e-ed1b3f032963/start
```

