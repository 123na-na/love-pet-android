# Debug Session: pet-page-blank-bug

## Status: [FIXED] - Waiting User Confirmation

## Bug Description
宠图页面显示空白，无法加载任何卡片内容。

## Hypotheses
| ID | Hypothesis | Status | Evidence |
|----|------------|--------|----------|
| H5 | initPetPage 未被调用 | ❌ REJECTED | `[H5] initPetPage CALLED, rootView=NOT_NULL` |
| H2 | gridPets (grid_pets) 为 null | ❌ REJECTED | `[H2] gridPets=NOT_NULL` |
| **H1** | **initializeDataSync() 数组越界** | **✅ CONFIRMED** | `ArrayIndexOutOfBoundsException: length=15; index=15` at PetDataManager.java:100 |
| H3 | 数据库返回空列表 | N/A | 异常先于此发生 |
| H4 | Fallback 未执行 | N/A | 异常后应执行 |

## Root Cause
`randomInt(0, array.length)` 返回范围是 `[0, length]`（包含length），但数组有效索引最大为 `length-1`。
当 NICKNAME_POOL 长度为15时，`randomInt(0, 15)` 可能返回15，导致越界。

## Fix Applied
**File**: `PetDataManager.java`
- `randomInt(0, MockPetData.CITY_POOL.length)` → `randomInt(0, MockPetData.CITY_POOL.length - 1)`
- `randomInt(0, MockPetData.TITLE_POOL.length)` → `randomInt(0, MockPetData.TITLE_POOL.length - 1)`
- `randomInt(0, MockPetData.CONTENT_POOL.length)` → `randomInt(0, MockPetData.CONTENT_POOL.length - 1)`
- `randomInt(0, MockPetData.NICKNAME_POOL.length)` → `randomInt(0, MockPetData.NICKNAME_POOL.length - 1)`
- `randomInt(0, MockPetData.COMMENT_CONTENT_POOL.length)` → `randomInt(0, MockPetData.COMMENT_CONTENT_POOL.length - 1)`

## Timeline
- [x] Step 1: Instrumentation added
- [x] Step 2: User reproduced issue + provided logs
- [x] Step 3: Analyzed logs → Found ArrayIndexOutOfBounds
- [x] Step 4: Confirmed hypothesis H1
- [x] Step 5: Applied fix (length → length - 1)
- [ ] Step 6: Verify fix (user testing)
- [ ] Step 7: Cleanup instrumentation

## Evidence Log
```
[H5] initPetPage CALLED, rootView=NOT_NULL
[H2] gridPets=NOT_NULL
[H1] loadDataFromDatabase START, dataManager=OK
EXCEPTION: java.lang.ArrayIndexOutOfBoundsException: length=15; index=15
        at PetDataManager.initializeDataSync(PetDataManager.java:100)
```
