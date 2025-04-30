package router.handler.impl;

import db.Database;
import model.Article;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import webserver.common.ContentType;
import webserver.http.request.HttpRequest;
import webserver.http.response.HttpResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public class MainPageHandler extends HtmlFileHandler {
    private static final Logger logger = LoggerFactory.getLogger(MainPageHandler.class);

    @Override
    public void handle(HttpRequest request, HttpResponse response) throws IOException {
        byte[] fileContent = loadFileContent("/index.html");
        byte[] processedContent = processContent(fileContent, ContentType.HTML, request);

        String htmlContent = new String(processedContent, StandardCharsets.UTF_8);

        Collection<Article> allArticles = Database.findAllArticle();

        if (!allArticles.isEmpty()) {
            List<Article> sortedArticles = new ArrayList<>(allArticles);
            sortedArticles.sort(Comparator.comparing(Article::getId).reversed());

            Article latestArticle = sortedArticles.get(0);

            htmlContent = viewArticle(htmlContent, latestArticle);
        }

        response.sendOk(ContentType.HTML, htmlContent.getBytes(StandardCharsets.UTF_8));
    }

    private String viewArticle(String htmlContent, Article article) {
        User author = Database.findUserById(article.getUserId());
        String authorName = author != null ? author.getName() : "알 수 없는 사용자";

        htmlContent = htmlContent.replace("<p class=\"post__account__nickname\">account</p>",
                "<p class=\"post__account__nickname\">" + authorName + "</p>");

        htmlContent = htmlContent.replace("<p class=\"post__article\">\n            게시글이 없습니다.\n          </p>",
                "<p class=\"post__article\">\n" + article.getContent() + "\n</p>");

        htmlContent = htmlContent.replace("<img class=\"post__img\" src=\"img/cloud.png\">",
                "<a href=\"/articles/" + article.getId() + "\"><img class=\"post__img\" src=\"img/cloud.png\"></a>");

        String prevLinkPattern = "<li class=\"nav__menu__item\">\n" +
                "              <a class=\"nav__menu__item__btn\" style=\"color: #ccc; cursor: not-allowed;\" href=\"javascript:void(0);\">\n" +
                "                <img\n" +
                "                  class=\"nav__menu__item__img\"\n" +
                "                  src=\"/img/ci_chevron-left.svg\"\n" +
                "                />\n" +
                "                이전 글\n" +
                "              </a>\n" +
                "            </li>";

        String nextLinkPattern = "<li class=\"nav__menu__item\">\n" +
                "              <a class=\"nav__menu__item__btn\" style=\"color: #ccc; cursor: not-allowed;\" href=\"javascript:void(0);\">\n" +
                "                다음 글\n" +
                "                <img\n" +
                "                  class=\"nav__menu__item__img\"\n" +
                "                  src=\"/img/ci_chevron-right.svg\"\n" +
                "                />\n" +
                "              </a>\n" +
                "            </li>";

        Article prevArticle = Database.findPreviousArticle(article.getId());
        Article nextArticle = Database.findNextArticle(article.getId());

        if (prevArticle != null) {
            String prevLinkReplacement = "<li class=\"nav__menu__item\">\n" +
                    "              <a class=\"nav__menu__item__btn\" href=\"/articles/" + prevArticle.getId() + "\">\n" +
                    "                <img\n" +
                    "                  class=\"nav__menu__item__img\"\n" +
                    "                  src=\"/img/ci_chevron-left.svg\"\n" +
                    "                />\n" +
                    "                이전 글\n" +
                    "              </a>\n" +
                    "            </li>";
            htmlContent = htmlContent.replace(prevLinkPattern, prevLinkReplacement);
        } else {
            // 이전글이 없는 경우 링크 비활성화
            String prevLinkReplacement = "<li class=\"nav__menu__item\">\n" +
                    "              <a class=\"nav__menu__item__btn\" style=\"color: #ccc; cursor: not-allowed;\" href=\"javascript:void(0);\">\n" +
                    "                <img\n" +
                    "                  class=\"nav__menu__item__img\"\n" +
                    "                  src=\"/img/ci_chevron-left.svg\"\n" +
                    "                />\n" +
                    "                이전 글\n" +
                    "              </a>\n" +
                    "            </li>";
            htmlContent = htmlContent.replace(prevLinkPattern, prevLinkReplacement);
        }

        if (nextArticle != null) {
            String nextLinkReplacement = "<li class=\"nav__menu__item\">\n" +
                    "              <a class=\"nav__menu__item__btn\" href=\"/articles/" + nextArticle.getId() + "\">\n" +
                    "                다음 글\n" +
                    "                <img\n" +
                    "                  class=\"nav__menu__item__img\"\n" +
                    "                  src=\"/img/ci_chevron-right.svg\"\n" +
                    "                />\n" +
                    "              </a>\n" +
                    "            </li>";
            htmlContent = htmlContent.replace(nextLinkPattern, nextLinkReplacement);
        } else {
            // 다음글이 없는 경우 링크 비활성화
            String nextLinkReplacement = "<li class=\"nav__menu__item\">\n" +
                    "              <a class=\"nav__menu__item__btn\" style=\"color: #ccc; cursor: not-allowed;\" href=\"javascript:void(0);\">\n" +
                    "                다음 글\n" +
                    "                <img\n" +
                    "                  class=\"nav__menu__item__img\"\n" +
                    "                  src=\"/img/ci_chevron-right.svg\"\n" +
                    "                />\n" +
                    "              </a>\n" +
                    "            </li>";
            htmlContent = htmlContent.replace(nextLinkPattern, nextLinkReplacement);
        }

        return htmlContent;
    }
}