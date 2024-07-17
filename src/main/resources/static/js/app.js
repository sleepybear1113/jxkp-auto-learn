const {createApp} = Vue;
let app = createApp({
    data() {
        return {
            tab: 1,
            userCookie: "",
            idCard: "",
            idCard1: "",
            password: "",
            password1: "",
            captcha: "",
            loadCaptcha: false,
            captchaImgSrc: "",
            userInfoDto: new UserInfoDto(),
            courseInfoDtoList: [],
            showLogin: false,
            t: new Date().getTime(),
        };
    },
    created() {
        this.getUserProfile();
    },
    methods: {
        login() {
            let url = "/login";
            let loginParams = {
                params: {
                    idCard: this.idCard,
                    password: this.password,
                    captcha: this.captcha,
                }
            };
            axios.get(url, loginParams).then(res => {
                let data = res.data.result;
                this.userInfoDto = new UserInfoDto(data);
                alert("登录成功！请点击“获取所有课程”");
                this.tab = 3;
            }).catch(() => {
            });
        },
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
            this.t = new Date().getTime();
            this.idCard1 = this.idCard;
            this.password1 = this.password;
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
                alert("开始学习中。。。");
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
        getRandom() {
            return Math.random();
        },
    },
    watch: {}
});
app.mount("#app");