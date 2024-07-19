import dayjs from "dayjs";
import utc from "dayjs/plugin/utc";

dayjs.extend(utc);

/* UTC TO LOCAL */
var datetimeUTC = dayjs().utc();
console.log( datetimeUTC.format() ); // 2024-03-01T13:42:51Z
var datetimeLocal = datetimeUTC.local();
console.log( datetimeLocal.format() ); // 2024-03-01T19:12:51+05:30

/* LOCAL TO UTC */
var datetimeLocal = dayjs();
console.log( datetimeLocal.format() ); // 2024-03-01T19:22:47+05:30
console.log( datetimeLocal.toISOString() ); // 2024-03-01T13:52:47.082Z
var datetimeUTC = datetimeLocal.utc();
console.log( datetimeUTC.format() ); // 2024-03-01T13:52:47Z