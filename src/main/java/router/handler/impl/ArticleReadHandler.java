package router.handler.impl;

import db.Database;
import model.Article;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import webserver.common.ContentType;
import webserver.http.request.HttpRequest;
import webserver.http.request.PathVariableRequestWrapper;
import webserver.http.response.HttpResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class ArticleReadHandler extends HtmlFileHandler {
    private static final Logger logger = LoggerFactory.getLogger(ArticleReadHandler.class);

    @Override
    public void handle(HttpRequest request, HttpResponse response) throws IOException {

        String articleIdStr = null;

        if (request instanceof PathVariableRequestWrapper) {
            PathVariableRequestWrapper pathRequest = (PathVariableRequestWrapper) request;
            articleIdStr = pathRequest.getPathVariable("id");
            logger.debug("경로 변수에서 게시글 ID 추출: {}", articleIdStr);
        } else {
            if (request.getQueryParameters().containsKey("id")) {
                articleIdStr = request.getQueryParameters().get("id").get(0);
                logger.debug("쿼리 파라미터에서 게시글 ID 추출: {}", articleIdStr);
            }
        }

        if (articleIdStr == null) {
            logger.error("게시글 ID가 없습니다.");
            response.send400("게시글 ID가 필요합니다.");
            return;
        }

        Long articleId;
        try {
            articleId = Long.parseLong(articleIdStr);
        } catch (NumberFormatException e) {
            logger.error("잘못된 게시글 ID 형식: {}", articleIdStr);
            response.send400("유효하지 않은 게시글 ID 형식입니다.");
            return;
        }

        Article article = Database.findArticleById(articleId);
        if (article == null) {
            logger.error("ID가 {}인 게시글을 찾을 수 없습니다.", articleId);
            response.send404();
            return;
        }

        byte[] fileContent = loadFileContent("/index.html");
        byte[] processedContent = processContent(fileContent, ContentType.HTML, request);

        String htmlContent = new String(processedContent, StandardCharsets.UTF_8);
        htmlContent = viewArticle(htmlContent, article);

        response.sendOk(ContentType.HTML, htmlContent.getBytes(StandardCharsets.UTF_8));
    }

    private String viewArticle(String htmlContent, Article article) {
        User author = Database.findUserById(article.getUserId());
        String authorName = author != null ? author.getName() : "알 수 없는 사용자";

        htmlContent = htmlContent.replace("<p class=\"post__account__nickname\">account</p>",
                "<p class=\"post__account__nickname\">" + authorName + "</p>");

        htmlContent = htmlContent.replace("<p class=\"post__article\">\n" +
                        "            게시글이 없습니다.\n" +
                        "          </p>",
                "<p class=\"post__article\">\n" + article.getContent() + "\n</p>");

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
