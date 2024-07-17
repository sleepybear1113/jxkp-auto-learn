const {createApp} = Vue;
let app = createApp({
    data() {
        return {
            userCookie: "",
            idCard: "",
            password: "",
            loadCaptcha: false,
            captchaImgSrc: "",
            userInfoDto: new UserInfoDto(),
            courseInfoDtoList: [],
            showLogin: false,
        }
    },
    created() {
        this.getUserProfile();
    },
    methods: {
        addUserCookie() {
            let url = "/addUserCookie";
            let addUserCookieParams = {
                params: {
                    cookie: this.userCookie.replaceAll("'", ""),
                }
            };
            axios.get(url, addUserCookieParams).then(res => {
                let data = res.data.result;
                this.userInfoDto = new UserInfoDto(data);
                console.log(this.userInfoDto);
            }).catch(() => {
            });
        },
        getCaptchaImg() {
            let url = "/getCaptchaImg";
            return `${url}?t=${new Date().getTime()}&idCard=${this.idCard}&password=${this.password}`;
        },
        getCaptcha() {
            if (!this.idCard || !this.password) {
                alert("请输入账号和密码！");
                return;
            }
            this.loadCaptcha = false;
            setTimeout(() => this.loadCaptcha = true, 500);
        },
        getUserProfile() {
            let url = "/getUserProfile";
            axios.get(url).then(res => {
                let data = res.data.result;
                this.userInfoDto = new UserInfoDto(data);
                console.log(this.userInfoDto);
            }).catch(() => {
            });
        },
        getPersonTotalLessons() {
            let url = "/getPersonTotalLessons";
            axios.get(url).then(res => {
                let data = res.data.result;
                this.courseInfoDtoList = mapToArray(data, CourseInfoDto);
                console.log(this.courseInfoDtoList);
            }).catch(() => {
            });
        },
        learn() {
            let url = "/learn";
            let kcIdList = [];
            this.courseInfoDtoList.forEach(item => {
                if (item.status === "已学完") {
                    return;
                }
                if (item.checked) {
                    kcIdList.push(item.kcId);
                }
            });

            if (kcIdList.length === 0) {
                alert("请选择课程！");
                return;
            }

            let learnParams = {
                params: {
                    kcIdList: kcIdList.join(","),
                }
            };
            axios.get(url, learnParams).then(res => {
                console.log(res.data.result);
            }).catch(() => {
            });
        },
        stop() {
            let url = "/stop";
            axios.get(url).then(res => {
                alert("正在停止学习中，请稍后。。。")
            }).catch(() => {
            });
        },
    },
    watch: {}
});
app.mount("#app");