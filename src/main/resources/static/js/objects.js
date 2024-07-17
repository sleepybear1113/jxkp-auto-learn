class UserInfoDto {
    constructor(props = {}) {
        this.idCard = props.idCard;
        this.password = props.password;
        this.cookie = props.cookie;
        this.lessonKcIdList = mapToArray(props.lessonKcIdList);
        this.name = props.name;
        this.tel = props.tel;
    }
}

class CourseInfoDto {
    constructor(props = {}) {
        this.checked = props.checked;
        this.kcId = props.kcId;
        this.name = props.name;
        this.learnedCount = props.learnedCount;
        this.totalLessonCount = props.totalLessonCount;
        this.hours = props.hours;
        this.status = props.status;
    }
}


function mapToArray(prop, Obj) {
    return prop ? prop.map(item => Obj ? new Obj(item) : item) : [];
}