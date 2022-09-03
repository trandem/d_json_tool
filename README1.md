# 1. Vấn đề
Khi viết **test** cho api **update** một **entity** được lưu trong **database** hoặc ở bất cứ đâu.
**Entity** này trong các sản phẩm phần mềm thường rất phức tạp :
- Có rất nhiều trường (> 20 trường)
- Lưu trữ vật lý tại nhiều bảng khác nhau
- Cấu trúc **entity** rất phức tạp bao gồm : object value, array value, ...

Để đảm bảo được **api** update đúng dữ liệu mong muốn, không **update** nhầm trường hay thêm sửa xóa bất kỳ **field** nào khác.
Chúng ta sẽ phải compare 2 **entity** trước và sau gọi **api** nhưng việc này có những nhược điểm sau:
- **Entity** quá phức tạp khiến việc compare bằng con người thường bị bỏ xót trường hoặc tốn quá nhiều lỗ lực để compare tất cả các trường
- Khi chúng ta sử dụng **library** để **compare** **json** của **output** object với **expected** object (lưu dạng json). Việc này 
sẽ không phải so sánh bằng con người nhưng khi thay đổi bất cứ thứ tự dữ liệu hay thêm, xóa dữ liệu trong **entity** thì sẽ phải sửa
toàn bộ các **file json expected** mặc dù các test vẫn chạy đúng. Việc này rất tốn thời gian khi bạn **maintain** một lượng **test** đủ lớn.
- Khi **compare** toàn bộ **object json** sẽ gây khó hiểu cho những thành viên mới khi đọc **test**, không biết chính xác **test** đang thay đổi 
giá trị gì.

# 2. Cách giải quyết.
Project này đề xuất phương pháp **compare** 2 **json object**, trích xuất sự khác biệt và lưu nó dưới dạng một cấu trúc **json** 
để dễ đọc hiểu.

Cấu trúc **json** được đề xuất như sau :
```json
{
  "updated": {
  },
  "inserted" : {
  },
  "deleted": {
  }
}
```

## 2.1. Giải thích cấu trúc output
### updated 
Chứa các **field** mang giá tri nguyên thủy, có thay đổi giá trị trước và sau updated.

### inserted
Chứa các **field** mang giá trị là **object** hoặc **array** được thêm mới vào **object** sau khi **update**

Các **field** mới xuất hiện tại **object** sau khi **update**, trước đó **object** không tồn tại **field** này.
### deleted
Chứa các **field** mang giá trị là **object** hoặc **array** được xóa đi trong **object** sau khi **update**

# 3 Sample Input/Output
Project sẽ không hỗ trợ kiểu array của array vì chưa biết **expected** cho trường hợp này như thế nào.

Project đã có test **coverage 93% line** nên có thể tin tưởng để dùng
## 3.1. Insert/Delete Object in Array
<details>
  <summary>before object</summary>

```json
{
  "plants": [
    {
      "plantId": "1",
      "name": "plant1"
    },
    {
      "plantId": "2",
      "name": "plant2"
    },
    {
      "plantId": "3",
      "name": "plant3"
    }
  ],
  "demtv": 11
}
```
</details>


<details>
  <summary>after object</summary>

```json
{
  "plants": [
    {
      "plantId": "1",
      "name": "plant11"
    },
    {
      "plantId": "3",
      "name": "plant3"
    },
    {
      "plantId": "4",
      "name": "plant4"
    }
  ],
  "demtv": 11
}
```

</details>

<details>
  <summary>diff output</summary>

```json
{
  "updated": {
    "plants.plantId.1.name": {
      "before": "plant1",
      "after": "plant11"
    }
  },
  "deleted": {
    "plants.plantId.2": {
      "plantId": "2",
      "name": "plant2"
    }
  },
  "inserted": {
    "plants.plantId.4": {
      "plantId": "4",
      "name": "plant4"
    }
  }
}
```
</details>

Project hỗ trợ **deleted** chứa **array** key bị xóa và **updated** chứa giá trị **before** update. Nếu dùng mode này thì sẽ được
output như sau:

<details>
  <summary>diff deleted arraykey, update contain before value</summary>

```json
{
  "updated": {
    "plants.plantId.1.name": {
      "before": "plant1",
      "after": "plant11"
    }
  },
  "deleted": {
    "keys": [
      "plants.plantId.2"
    ]
  },
  "inserted": {
    "plants.plantId.4": {
      "plantId": "4",
      "name": "plant4"
    }
  }
}
```
</details>

Khi sử dụng các **lib** để compare sự giống nhau của 2 **json** thì nó sẽ rất hạn chế khi **compare** array vậy nên chúng tôi khuyến khích sử dụng 
mode chỉ hiện **key** bị xóa trong **deleted** như sau :


<details>
  <summary>diff deleted show key before, update contain before value</summary>

```json
{
  "updated": {
    "plants.plantId.1.name": {
      "before": "plant1",
      "after": "plant11"
    }
  },
  "deleted": {
    "plants.plantId.2": 1
  },
  "inserted": {
    "plants.plantId.4": {
      "plantId": "4",
      "name": "plant4"
    }
  }
}
```
</details>

## 3.2. Sample Complex Json Object

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
    "outerArr.id.dota.info.version": {
      "before": "1.0",
      "after": "2.0"
    },
    "outerArr.id.tc.info.innerArr.id.1.game": {
      "before": "tc1",
      "after": "tc11"
    }
  },
  "deleted": {
    "outerArr.id.dota.info.innerArr": 1,
    "outerArr.id.tc.info.innerArr.id.2": 1,
    "publisher": 1,
    "outerArr.id.lol": 1
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
