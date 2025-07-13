package cn.sleepybear.jxkpautolearn.logic;

import cn.sleepybear.cacher.cache.ExpireWayEnum;
import cn.sleepybear.jxkpautolearn.advice.ResultCodeConstant;
import cn.sleepybear.jxkpautolearn.dto.CourseInfoDto;
import cn.sleepybear.jxkpautolearn.dto.LessonInfoDto;
import cn.sleepybear.jxkpautolearn.dto.UserInfoDto;
import cn.sleepybear.jxkpautolearn.exception.FrontException;
import cn.sleepybear.jxkpautolearn.utils.CommonUtils;
import cn.sleepybear.jxkpautolearn.utils.CookieUtils;
import cn.sleepybear.jxkpautolearn.utils.MyCookieJar;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.apache.commons.collections4.CollectionUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * There is description
 *
 * @author sleepybear
 * @date 2024/07/16 15:32
 */
@Component
@Slf4j
public class AutoLearnLogic {
    public static final String DOMAIN = "zy.jxkp.net";
    public static final String BASE_URL = "http://" + DOMAIN;

    public static void main(String[] args) {
        String kcId = "37";
        String c = "CfDJ8PCJEpvrLHBNjlXrn25Ppqs7T2-XrC8fS5mvbyWcsTbR1WahfAuFqfHGnUcGlbtDOEiG__cto9ed9JN8JPt9P1Q4_UtPCp6CwcmeYeEJLwoS6jUxUKKV01ByszYR9TYpz9I2r9D3nU_ki9NZUn2njk0e7uQevbWkObUHpTRrD_Vro6ip4LF34gv1eM8b8IzMH3jqIjQIkCx3ZuZ2xkg-gA_FP4lioy03zD8SbBs8fCvtJCozcIXn-vBz6SumlD6rTNvbO4SCxDrnSio65B7btQ_cgGMDznunJer8MhTqMBZsTZJOUY1cIByVliLTFq0DaLXNyiGLhjPrC6KkluZ7IiCNEu2U3eNzrIazaS-VtNWotiGTaGlzlqPB5BS22MGF6DDpF4I641sc6qBxI4BnKTM4t_I29Yebu4cYQhvrDgs5uqtqNuq4x4HvJWdesDePXajl0cRP-pH-bPLg189aOVJiIqqRUrEj3nZ08HmW2tm18BTj9xKAsr2v89vb08_SXvpSd3ZQ6OARGZ-1mrTXJWI";

        UserInfoDto userInfoDto = new UserInfoDto();
        MyCookieJar myCookieJar = new MyCookieJar();
        Cookie cookie = new Cookie.Builder()
                .name(".AspNetCore.Cookies") // 设置Cookie的名称
                .value(c)
                .domain("zy.jxkp.net") // 设置Cookie的域
                .path("/") // 设置Cookie的路径
                .build();
        myCookieJar.addCookie(cookie);
        userInfoDto.setMyCookieJar(myCookieJar);
        userInfoDto.setLessonKcIdList(List.of(kcId));

        boolean login = getUserProfile(userInfoDto);
        if (!login) {
            return;
        }

        List<CourseInfoDto> personTotalLessons = getPersonTotalLessons(userInfoDto);
        personTotalLessons.forEach(courseInfoDto -> log.info(courseInfoDto.toString()));

//        learn(userInfoDto);
    }

    /**
     * 获取主页的验证码 token
     *
     * @param userInfoDto userInfoDto
     */
    public static void getHomePage(UserInfoDto userInfoDto) {
        if (userInfoDto == null || userInfoDto.getIdCard() == null || userInfoDto.fetchPwd() == null) {
            throw new FrontException("用户登录账号密码不能为空！");
        }
        log.info("正在请求主页...");
        String responseBody = getResponseBodyString(BASE_URL, userInfoDto, null);
        Document document = Jsoup.parse(responseBody);
        for (Element element : document.select("input")) {
            if (element.outerHtml().contains("__RequestVerificationToken")) {
                userInfoDto.setCaptchaToken(element.attr("value"));
                break;
            }
        }
        log.info("请求主页成功！");
    }

    public static byte[] getCaptchaToken(UserInfoDto userInfoDto) {
        log.info("正在请求验证码...");
        String url = BASE_URL + "/home/ValidateCode";

        byte[] body = getResponseBody(url, userInfoDto, null);
        if (body == null) {
            throw new FrontException(ResultCodeConstant.CodeEnum.CANNOT_CONNECT_TO_SERVER, "无法连接至服务器！");
        }
        log.info("请求验证码成功！");
        return body;
    }

    public static void login(UserInfoDto userInfoDto, String captcha) {
        log.info("正在登录...");
        String url = BASE_URL + "/Login/AjaxDoLogin";
        RequestBody formBody = new FormBody.Builder()
                .add("Uname", userInfoDto.getIdCard())
                .add("pass", userInfoDto.fetchPwd())
                .add("valcode", captcha)
                .add("__RequestVerificationToken", userInfoDto.getCaptchaToken())
                .build();

        String responseBody = getResponseBodyString(url, userInfoDto, formBody);
        if (!responseBody.contains("true")) {
            // 登录失败，抛出异常，提示用户
            throw new FrontException(ResultCodeConstant.CodeEnum.CANNOT_CONNECT_TO_SERVER, responseBody);
        }

        getUserProfile(userInfoDto);
        CookieUtils.USER_CACHER.put(userInfoDto.getKey(), userInfoDto, 300L * 1000, ExpireWayEnum.AFTER_ACCESS);
        CookieUtils.setWebUserCookie(userInfoDto.getKey(), CookieUtils.COOKIE_MAX_AGE);
    }

    /**
     * 获取用户信息
     *
     * @param userInfoDto userInfoDto
     * @return 是否成功获取
     */
    public static boolean getUserProfile(UserInfoDto userInfoDto) {
        String url = BASE_URL + "/Person/EditProfile";
        log.info("正在请求获取用户信息...");

        String responseBody = getResponseBodyString(url, userInfoDto, null);

        if (responseBody.contains("请输入登录密码")) {
            throw new FrontException(ResultCodeConstant.CodeEnum.CANNOT_CONNECT_TO_SERVER, "登录失效！请重新登录");
        }

        Document doc = Jsoup.parse(responseBody);
        Element nameElement = doc.getElementById("Person_Name");
        Element telElement = doc.getElementById("Person_Tel");
        Element idCardElement = doc.getElementById("PersonExInfo_SfNumber");

        String name = nameElement == null ? null : decodeHtmlEntities(nameElement.attr("value"));
        String tel = telElement == null ? null : decodeHtmlEntities(telElement.attr("value"));
        String idCard = idCardElement == null ? null : decodeHtmlEntities(idCardElement.attr("value"));

        userInfoDto.setName(name);
        userInfoDto.setTel(tel);
        userInfoDto.setIdCard(idCard);

        log.info("请求获取用户信息成功，用户信息：{} - {}", userInfoDto.getName(), userInfoDto.getTel());
        return true;
    }

    public static List<CourseInfoDto> getPersonTotalLessons(UserInfoDto userInfoDto) {
        String url = BASE_URL + "/Person";
        log.info("正在请求获取用户课程列表...");

        List<CourseInfoDto> courseInfoDtoList = new ArrayList<>();

        String responseBody = getResponseBodyString(url, userInfoDto, null);

        String html = decodeHtmlEntities(responseBody);
        if (html.contains("请输入登录密码")) {
            throw new FrontException(ResultCodeConstant.CodeEnum.CANNOT_CONNECT_TO_SERVER, "登录失效！请重新登录");
        }
        Document doc = Jsoup.parse(html);
        Elements tables = doc.select("table");
        if (CollectionUtils.isEmpty(tables)) {
            return courseInfoDtoList;
        }

        Element table = null;
        for (Element element : tables) {
            if (element.text().contains("课程名称")) {
                table = element;
                break;
            }
        }

        if (table == null) {
            return courseInfoDtoList;
        }

        Elements trs = table.select("tr");
        if (CollectionUtils.isEmpty(trs)) {
            return courseInfoDtoList;
        }

        for (Element tr : trs) {
            CourseInfoDto courseInfoDto = new CourseInfoDto();
            courseInfoDtoList.add(courseInfoDto);

            Elements tds = tr.select("td");
            for (int i = 0; i < tds.size(); i++) {
                Element td = tds.get(i);
                switch (i) {
                    case 0:
                        courseInfoDto.setName(td.text());
                        courseInfoDto.setKcId(td.select("a").attr("href").split("=")[1]);
                        break;
                    case 1:
                        courseInfoDto.setLearnedCount(Integer.parseInt(td.text()));
                        break;
                    case 2:
                        courseInfoDto.setTotalLessonCount(Integer.parseInt(td.text()));
                        break;
                    case 3:
                        courseInfoDto.setHours(td.text());
                        break;
                    case 4:
                        courseInfoDto.setStatus(td.text());
                        break;
                    default:
                        break;
                }
            }
        }

        courseInfoDtoList.removeIf(CourseInfoDto::empty);
        return courseInfoDtoList;
    }

    public static void learn(UserInfoDto userInfoDto) {
        if (!Boolean.TRUE.equals(userInfoDto.getStop())) {
            throw new FrontException("当前有正在进行中的学习任务，无法重叠学习！");
        }

        userInfoDto.setStop(false);
        updateUserInfoDtoCache(userInfoDto);
        List<String> lessonKcIdList = userInfoDto.getLessonKcIdList();
        lessonKcIdList = new ArrayList<>(lessonKcIdList);
        for (String kcId : lessonKcIdList) {
            // 先获取要学习的课程列表的 HTML
            String lessonHTML = getLessonHTML(kcId, userInfoDto);
            if (lessonHTML == null) {
                continue;
            }

            // 解析课程列表的 HTML，获取课程的学习和未学习信息
            List<LessonInfoDto> lessonTrList = getLessonTrList(lessonHTML);
            for (LessonInfoDto lessonInfoDto : lessonTrList) {
                log.info(lessonInfoDto.toString());
            }

            if (CollectionUtils.isEmpty(lessonTrList)) {
                continue;
            }

            for (LessonInfoDto lessonInfoDto : lessonTrList) {
                if (lessonInfoDto.isFinished()) {
                    log.info("已完成：{}", lessonInfoDto.getLessonName());
                    continue;
                }

                String kjId = lessonInfoDto.getKcId();

                CommonUtils.sleep(1000);

                String kjHTML = getKjHTML(kjId, userInfoDto);
                String md5 = getKjMd5(kjHTML);

                Integer learnedCount = 30;
                int leftLessonCount = (lessonInfoDto.getTotalLessonCount() - lessonInfoDto.getLearnedCount()) * 60;
                boolean end = false;
                while (true) {
                    if (Boolean.TRUE.equals(userInfoDto.getStopping())) {
                        userInfoDto.setStop(true);
                        userInfoDto.setStopping(false);
                        log.info("学习结束！");
                        return;
                    }

                    log.info("等待 30 秒.....");
                    for (int i = 0; i < 30; i++) {
                        updateUserInfoDtoCache(userInfoDto);
                        if (Boolean.TRUE.equals(userInfoDto.getStopping())) {
                            userInfoDto.setStop(true);
                            userInfoDto.setStopping(false);
                            log.info("学习结束！");
                            return;
                        }
                        CommonUtils.sleep(1000);
                    }
                    updateKjLog(md5, userInfoDto, String.valueOf(learnedCount), kjId);

                    if (end) {
                        break;
                    }

                    learnedCount += 30;
                    if (learnedCount > leftLessonCount) {
                        learnedCount = leftLessonCount;
                        end = true;
                    }

                }
            }
        }

        userInfoDto.setStop(true);
        userInfoDto.setStopping(false);
        log.info("学习结束！");
    }

    public static void updateKjLog(String md5, UserInfoDto userInfoDto, String t, String kjId) {
        log.info("正在更新课件学习记录 kjId = {}, t = {}, md5 = {}...", kjId, t, md5);
        // 用 okhttp 发起请求
        String url = BASE_URL + "/Person/UpdateKjLog";

        RequestBody formBody = new FormBody.Builder()
                .add("md5", md5)
                .add("t", t)
                .add("kjid", kjId)
                .build();

        String responseBody = getResponseBodyString(url, userInfoDto, formBody);
        if (responseBody.contains("请输入登录密码")) {
            log.error("登录失效！");
            userInfoDto.setStop(true);
            userInfoDto.setStopping(false);
            throw new FrontException(ResultCodeConstant.CodeEnum.CANNOT_CONNECT_TO_SERVER, "登录失效！");
        }
        log.info(responseBody);
    }

    public static String getKjMd5(String html) {
        if (html == null) {
            return null;
        }
        String[] lines = html.split("\n");
        String md5 = null;
        for (String line : lines) {
            if (line.contains("var md5 = ")) {
                md5 = line.split("var md5 = ")[1].split(";")[0].replace("'", "");
                break;
            }
        }
        return md5;
    }

    public static String getLessonHTML(String kcId, UserInfoDto userInfoDto) {
        log.info("正在请求获取课程 kcId = {}...", kcId);
        String url = BASE_URL + "/Person/MyKjs?kcid=%s".formatted(kcId);
        String responseBody = getResponseBodyString(url, userInfoDto, null);
        log.info("请求获取课程成功 kcId = {}", kcId);
        return decodeHtmlEntities(responseBody);
    }

    public static List<LessonInfoDto> getLessonTrList(String lessonHTML) {
        Document doc = Jsoup.parse(lessonHTML);
        Elements links = doc.select("table");
        if (CollectionUtils.isEmpty(links)) {
            return new ArrayList<>();
        }

        Element table = null;
        for (Element link : links) {
            // 如果包含“课件名”则认为是课程表格
            if (link.text().contains("课件名")) {
                table = link;
                break;
            }
        }
        if (table == null) {
            log.info("未获取到课程列表");
            return new ArrayList<>();
        }

        List<LessonInfoDto> lessonInfoDtoList = new ArrayList<>();
        Elements trList = table.select("tr");
        for (Element tr : trList) {
            LessonInfoDto lessonInfoDto = new LessonInfoDto();
            lessonInfoDtoList.add(lessonInfoDto);

            for (int i = 0; i < tr.select("td").size(); i++) {
                Element td = tr.select("td").get(i);
                switch (i) {
                    case 0:
                        lessonInfoDto.setLessonName(td.text());
                        lessonInfoDto.setUrl(td.select("a").attr("href"));
                        break;
                    case 1:
                        lessonInfoDto.setLearnedCount(Integer.parseInt(td.text()));
                        break;
                    case 2:
                        lessonInfoDto.setTotalLessonCount(Integer.parseInt(td.text()));
                        break;
                    case 3:
                        lessonInfoDto.setStatus(td.text());
                        break;
                    default:
                        break;
                }
            }
        }

        lessonInfoDtoList.removeIf(LessonInfoDto::empty);
        log.info("获取到课程列表数量：{}", lessonInfoDtoList.size());
        return lessonInfoDtoList;
    }

    public static String getKjHTML(String kjId, UserInfoDto userInfoDto) {
        String url = BASE_URL + "/Person/Play/%s".formatted(kjId);
        log.info("正在请求获取视频课件 kjId = {}...", kjId);
        String responseBody = getResponseBodyString(url, userInfoDto, null);
        log.info("请求获取视频课件成功 kjId = {}", kjId);
        return decodeHtmlEntities(responseBody);

    }

    public static String decodeHtmlEntities(String input) {
        if (input == null) {
            return null;
        }
        // 匹配 &#xXXXX; 的正则表达式
        Pattern pattern = Pattern.compile("&#x([0-9A-Fa-f]+);");
        Matcher matcher = pattern.matcher(input);
        StringBuilder decoded = new StringBuilder();

        while (matcher.find()) {
            // 获取 HTML 实体的代码
            String hexCode = matcher.group(1);
            // 转换为对应的字符
            char decodedChar = (char) Integer.parseInt(hexCode, 16);
            // 替换匹配的部分
            matcher.appendReplacement(decoded, Character.toString(decodedChar));
        }
        matcher.appendTail(decoded);

        // 返回解码后的字符串
        return decoded.toString();
    }

    public static String getResponseBodyString(String url, UserInfoDto userInfoDto, RequestBody requestBody) {
        try (Response response = getResponse(url, userInfoDto, requestBody)) {
            if (!response.isSuccessful()) {
                throw new FrontException(ResultCodeConstant.CodeEnum.CANNOT_CONNECT_TO_SERVER, 
                    "无法连接至服务器！状态码：" + response.code());
            }
            ResponseBody body = response.body();
            if (body == null) {
                throw new FrontException(ResultCodeConstant.CodeEnum.CANNOT_CONNECT_TO_SERVER, 
                "无法连接至服务器！响应体为空");
            }
            return body.string();
        } catch (IOException e) {
            throw new FrontException(ResultCodeConstant.CodeEnum.CANNOT_CONNECT_TO_SERVER, 
            "连接服务器失败：" + e.getMessage());
        }
    }

    private static Response getResponse(String url, UserInfoDto userInfoDto, RequestBody requestBody) throws IOException {
        Request request = requestBody != null
            ? new Request.Builder().url(url).post(requestBody).build()
            : new Request.Builder().url(url).build();
        
        OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(3000, TimeUnit.MILLISECONDS)
            .readTimeout(10 * 1000, TimeUnit.MILLISECONDS)
            .cookieJar(userInfoDto.getMyCookieJar())
            .build();
        
        return client.newCall(request).execute();
    }

    public static byte[] getResponseBody(String url, UserInfoDto userInfoDto, RequestBody requestBody) {
        Request request;
        if (requestBody != null) {
            request = new Request.Builder().url(url).post(requestBody).build();
        } else {
            request = new Request.Builder().url(url).build();
        }
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(3000, TimeUnit.MILLISECONDS)
                .readTimeout(10 * 1000, TimeUnit.MILLISECONDS)
                .cookieJar(userInfoDto.getMyCookieJar())
                .build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new FrontException(ResultCodeConstant.CodeEnum.CANNOT_CONNECT_TO_SERVER, "无法连接至服务器！状态码：" + response.code());
            }

            ResponseBody body = response.body();
            if (body == null) {
                throw new FrontException(ResultCodeConstant.CodeEnum.CANNOT_CONNECT_TO_SERVER, "无法连接至服务器！状态码：" + response.code());
            }
            return body.bytes();
        } catch (IOException e) {
            return null;
        }
    }

    public static void updateUserInfoDtoCache(UserInfoDto userInfoDto) {
        long now = System.currentTimeMillis();
        UserInfoDto cache = CookieUtils.USER_CACHER.get(userInfoDto.getKey());
        if (cache == null) {
            return;
        }
        cache.setLastLearnTime(now);
    }
}