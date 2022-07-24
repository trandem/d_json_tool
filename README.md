# Sample Input/Output


<details>
  <summary>before complex object</summary>

```json
{
  "project": "json_diff",
  "publisher": {
    "name": "garena",
    "year": 2010
  },
  "outerArr": [
    {
      "id": "lol",
      "info": {
        "version": "1.0",
        "innerArr": [
          {
            "id": 1,
            "game": "lol"
          }
        ]
      }
    },
    {
      "id": "tc",
      "info": {
        "version": "1.0",
        "innerArr": [
          {
            "id": 1,
            "game": "tc1"
          },
          {
            "id": 2,
            "game": "tc2"
          }
        ]
      }
    },
    {
      "id": "dota",
      "info": {
        "version": "1.0",
        "innerArr": [
          {
            "id": 1,
            "game": "dota"
          }
        ]
      }
    }
  ]
}

```

</details>


<details>
  <summary>after complex object</summary>

```json
{
  "project": "json_diff",
  "publisher": null,
  "outerArr": [
    {
      "id": "lol1",
      "info": {
        "version": "1.0",
        "innerArr": [
          {
            "id": 1,
            "game": "lol"
          }
        ]
      }
    },
    {
      "id": "dota",
      "info": {
        "version": "2.0",
        "innerArr": []
      }
    },
    {
      "id": "tc",
      "info": {
        "version": "1.0",
        "innerArr": [
          {
            "id": 1,
            "game": "tc11"
          },
          {
            "id": 3,
            "game": "tc3"
          }
        ]
      }
    }
  ]
}
```

</details>



<details>
  <summary>diff output</summary>

```json
{
  "updated": {
    "outerArr.id.dota.info.version": "2.0",
    "outerArr.id.tc.info.innerArr.id.1.game": "tc11"
  },
  "deleted": {
    "outerArr.id.dota.info.innerArr": [
      {
        "id": 1,
        "game": "dota"
      }
    ],
    "outerArr.id.tc.info.innerArr.id.2": {
      "id": 2,
      "game": "tc2"
    },
    "publisher": {
      "name": "garena",
      "year": 2010
    },
    "outerArr.id.lol": {
      "id": "lol",
      "info": {
        "version": "1.0",
        "innerArr": [
          {
            "id": 1,
            "game": "lol"
          }
        ]
      }
    }
  },
  "inserted": {
    "outerArr.id.lol1": {
      "id": "lol1",
      "info": {
        "version": "1.0",
        "innerArr": [
          {
            "id": 1,
            "game": "lol"
          }
        ]
      }
    },
    "outerArr.id.tc.info.innerArr.id.3": {
      "id": 3,
      "game": "tc3"
    }
  }
}
```

</details>
