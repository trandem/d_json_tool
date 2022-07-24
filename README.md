# Sample Input/Output


<details>
  <summary>before object</summary>

```json
{
  "employees": {
    "employee": [
      {
        "employId": "1",
        "firstName": "Tom",
        "lastName": "Cruise",
        "photo": "https://pbs.twimg.com/profile_images/735509975649378305/B81JwLT7.jpg"
      },
      {
        "employId": "2",
        "firstName": "Maria",
        "lastName": "Sharapova",
        "photo": "https://pbs.twimg.com/profile_images/786423002820784128/cjLHfMMJ_400x400.jpg"
      },
      {
        "employId": "3",
        "firstName": "James",
        "lastName": "Bond",
        "photo": "https://pbs.twimg.com/profile_images/664886718559076352/M00cOLrh.jpg"
      }
    ]
  },
  "number":["1","2","3","4"]
}
```

</details>


<details>
  <summary>after object</summary>

```json
{
  "employees": {
    "employee": [
      {
        "employId": "1",
        "firstName": "Tom2",
        "lastName": "Cruise",
        "photo": "https://pbs.twimg.com/profile_images/735509975649378305/B81JwLT7.jpg"
      },
      {
        "employId": "10",
        "firstName": "Tom2",
        "lastName": "Cruise",
        "photo": "https://pbs.twimg.com/profile_images/735509975649378305/B81JwLT7.jpg"
      },
      {
        "employId": "3",
        "firstName": "James",
        "photo": "https://pbs.twimg.com/profile_images/664886718559076352/M00cOLrh.jpg"
      },
      {
        "employId": "4",
        "firstName": "James",
        "lastName": "Bond",
        "photo": "https://pbs.twimg.com/profile_images/664886718559076352/M00cOLrh.jpg"
      }
    ]
  },
  "number":["1","5","6"],
  "demtv":333
}
```

</details>



<details>
  <summary>diff output</summary>

```json

{
  "updated": {
    "employees.employee.employId.1.firstName": "Tom2"
  },
  "inserted": {
    "employees.employee.employId.4": {
      "employId": "4",
      "firstName": "James",
      "lastName": "Bond",
      "photo": "https://pbs.twimg.com/profile_images/664886718559076352/M00cOLrh.jpg"
    },
    "employees.employee.employId.10":  {
      "employId": "10",
      "firstName": "Tom2",
      "lastName": "Cruise",
      "photo": "https://pbs.twimg.com/profile_images/735509975649378305/B81JwLT7.jpg"
    },
    "demtv": 333,
    "number": ["5","6"]
  },
  "deleted": {
    "number": ["2","3","4"],
    "employees.employee.employId.2": {
      "employId": "2",
      "firstName": "Maria",
      "lastName": "Sharapova",
      "photo": "https://pbs.twimg.com/profile_images/786423002820784128/cjLHfMMJ_400x400.jpg"
    },
    "employees.employee.employId.3.lastName": "Bond"

  }
}
```

</details>
