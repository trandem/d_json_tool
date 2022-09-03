# Compare two Json 

## 1. Giới thiệu

Trong quá trình phát triển các sản phẩm phần mềm, việc kiểm thử là bước vô cùng quan trọng.
Không có kiểm thử thì phần mềm phát triển không đủ sự tin tưởng.

Hiện nay với sự phức tạp của các nghiệp vụ do đó các **Entity** trong các sản phẩm phần mềm ngày 
càng trở lên phức tạp :
- Có rất nhiều trường (field) > 20 fields
- Lưu trữ vật lý tại nhiều bảng khác nhau hoặc nhiều loại DB khác nhau
- Cấu trúc của một **Entity** thường sẽ phúc tạp: chứa nhiều object khác, array object, ...

Khi viết **iteration test** thông thường chúng ta sẽ chỉ thực hiện **assert** cho một  vài trường nghĩ là sẽ thay đổi sau khi gọi 
qua **API** điều này là đúng nhưng chưa đủ.

Ví dụ khi ta lập trình gọi **API** cho phép thay đổi giá của sản phẩm nhưng không may trong quá trình đó lại thực hiện cập nhật nhầm
một thuộc tính nào đó như số lượng tồn kho. Việc này là rất nguy hiểm thường sảy ra khi **code** của **project** đã quá lớn và
là người mới **join** vào quá trình phát triển chưa nắm rõ. Khi thực hiện **testing** nếu chỉ thực hiện việc **assert** cho trường 
giá thì vẫn chưa đủ.

Một cách dễ dàng có thể khắc phục là thực hiện **compare** toàn bộ 2 **entity** dưới dạng **json**. **Expected** sẽ
được **query** từ **DB**, **actual** sẽ được lưu trong 1 file text. Nếu 2 **object** này giống nhau hoàn toàn thì có 
thể chắc chắn được **API** chúng ta đang hoạt động đúng.

Thư viện sử dụng có thể là 
```json
        <dependency>
            <groupId>org.skyscreamer</groupId>
            <artifactId>jsonassert</artifactId>
            <version>1.5.0</version>
        </dependency>
```
Hoặc dùng chính project để thực hiện **compare** này.

Cách này khá dễ thực hiện nhưng sẽ có một số nhược điểm :
- Khi ta thêm, xóa trường trong **Entity** thì tất cả các **file** json sẽ bị ảnh hưởng và bắt buộc phải thay đổi theo.
- Khi đọc test thì người đọc sẽ rất khó biết được **API** thực sự thay đổi điều gì

## 2. Cách giải quyết
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

Project sẽ sử dụng thuật toán **DFS** để duyệt qua và thực hiện **compare** 2 **Json**. Project sử dụng **Json** thay vì
**reflection** vì **Json** sẽ dễ đọc hơn khi thực hiện lưu kết quả của **compare**.

## 2.1. Giải thích cấu trúc output
### updated
Chứa các **field** mang giá tri nguyên thủy, có thay đổi giá trị trước và sau updated.

### inserted
Chứa các **field** mang giá trị là **object** hoặc **array** được thêm mới vào **object**

Các **field** mới xuất hiện tại **object** sau khi thực hiện action, trước đó **object** không tồn tại **field** này.

### deleted
Chứa các **field** mang giá trị là **object** hoặc **array** được xóa đi trong **object** sau khi **update**

## 2.2. Cách sử dụng
Chi tiết về cách sử dụng tham khảo các test được viết sẵn :
- [InsertObjectBuilderTest](src/test/java/dem/tool/diff/InsertObjectBuilderTest.java)

Ví dụ ta có object sau :

Entity trước khi thực hiện action.

<details>
  <summary>object_before_not_have_contract_field</summary>

```json
{
  "employee":
  {
    "id": "1212",
    "fullName":"John Miles",
    "age": 35
  },
  "dem" : 16
}
```
</details>

Entity sau khi thực hiện action

<details>
  <summary>object_after_add_contract_object</summary>

```json
{
  "employee": {
    "employId": "12122",
    "fullName": "John 1",
    "age": 35,
    "contact": {
      "email": "john@xyz.com",
      "phone": "9999999"
    }
  }
}
```
</details>

Sau khi thực hiện compare sẽ được kết quả như sau :

```java

 var diffJson = new DDiffJsonBuilder()
                .insertBuilder( new InsertObjectBuilder())
                .updateBuilder(new UpdateObjectBuilder())
                .deleteBuilder(new DeleteFlattenKeyBuilder())
                .build();

diffJson.diffScan(beforeObject, afterObject);

var output = diffJson.toJsonFormatString();

```

<details>
  <summary>diff output</summary>

```json
{
  "updated": {
    "employee": {
      "fullName": "John 1"
    }
  },
  "inserted": {
    "employee": {
      "employId": "12122",
      "contact": {
        "email": "john@xyz.com",
        "phone": "9999999"
      }
    }
  },
  "deleted": {
    "employee.id": 1,
    "dem": 1
  }
}
```
</details>

Với **Object** nhỏ thì việc thực hiện chạy qua **DiffJson** thì không nhận thấy được ưu điểm nhưng nếu là một **Object** lớn và
phức tạp, khi thực hiện **API** chỉ thay đổi vài trường thì sẽ thấy được lợi ích.

2.3. Exclude fields
Khi thực hiện **API** có một số trường chúng ta không muốn **compare** như **updated timestamp**, trường **timestamp** 
này có thể có ở tất cả **object** nên việc loại trừ được các trường này cũng rất cần thiết.

Before Object :
<details>
  <summary>Before Object</summary>

```json
{
  "employee":
  {
    "id": "1212",
    "updateTime" : 132245124312,
    "fullName":"John Miles",
    "age": 35
  },
  "dem" : 16,
  "createTime" : 12412412412
}
```

</details>
After Object :

<details>
  <summary>After Object</summary>

```json
{
  "employee": {
    "employId": "12122",
    "fullName": "John 1",
    "updateTime" : 13225124312,
    "age": 35,
    "contact": {
      "email": "john@xyz.com",
      "phone": "9999999"
    }
  },
   "createTime" : 1241312412
}
```
</details>

Code exclude **timestamp**
```java
        JsonNode beforeObject = DJacksonCommon.loadJsonFromFile("before_timestamp.json");
        JsonNode afterObject = DJacksonCommon.loadJsonFromFile("after_timestamp.json");
        var diffCompare = new DDiffJsonBuilder()
                .insertBuilder( new InsertObjectBuilder())
                .updateBuilder(new UpdateObjectBuilder())
                .excludeCompareFieldPath("createTime")
                .excludeCompareFieldPath("employee.updateTime")
                .deleteBuilder(new DeleteFlattenKeyBuilder())
                .build();

        diffCompare.diffScan(beforeObject, afterObject);

        String output = diffCompare.toJsonFormatString();
        System.out.println(output);
```

Output Object :
<details>
  <summary>Output Object</summary>

```json
{
  "updated": {
    "employee": {
      "fullName": "John 1"
    }
  },
  "deleted": {
    "employee.id": 1,
    "dem": 1
  },
  "inserted": {
    "employee": {
      "employId": "12122",
      "contact": {
        "email": "john@xyz.com",
        "phone": "9999999"
      }
    }
  }
}
```
</details>

## 2.4. Insert/Delete Object In array
Một tính năng nữa của **DDiff** là có thể xác định được object nào đã bị xóa, thêm vào, update trong **json array**


Before Object :
<details>
  <summary>Before Object</summary>

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

After Object :

<details>
  <summary>After Object</summary>

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

Code for object have array :
```java
        JsonNode beforeObject = DJacksonCommon.loadJsonFromFile("array_json_sample/before_have_array_plant.json");
        JsonNode afterObject = DJacksonCommon.loadJsonFromFile("array_json_sample/after_have_array_plant.json");

        diffJson.registerObjectKeyInArrayByPath("plants","plantId");
        diffJson.diffScan(beforeObject, afterObject);

        String output = diffJson.toJsonFormatString();
```

Output Object :
<details>
  <summary>Output Object</summary>

```json
{
  "updated": {
    "plants": [
      {
        "plantId": "1",
        "name": "plant11"
      }
    ]
  },
  "inserted": {
    "plants": [
      {
        "plantId": "4",
        "name": "plant4"
      }
    ]
  },
  "deleted": {
    "plants.plantId.2": 1
  }
}
```
</details>

# 3. Tổng kết.
Các cách dùng kỹ hơn về thư viện này vui lòng tham khảo tại thư mục test. Test của Project đạt đến **83% line** vậy nên có thể 
tin tưởng để sử dụng. 

Mọi người sử dụng nếu có **issue** thì để lại cho tác giả **fix** nhé. Cảm ơn mọi người, nếu hay xin cho tác giả một **star** 
trên github nhé. 
