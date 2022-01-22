<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="uv" tagdir="/WEB-INF/tags" %>

<c:if test="${theme == 'system'}">
    <script>
        (function() {
            let mediaQueryDark = window.matchMedia('(prefers-color-scheme: dark)');
            if (mediaQueryDark.matches) {
                document.querySelector("html").classList.add("tw-dark");
            }
            try {
                mediaQueryDark.addEventListener("change", function () {
                    document.querySelector("html").classList.toggle("tw-dark");
                });
            } catch (error) {
                // safari (https://stackoverflow.com/a/60000747)
                try {
                    mediaQueryDark.addListener(function() {
                        document.querySelector("html").classList.toggle("tw-dark");
                    });
                } catch (error2) {
                    console.info("could not add mediaQuery listener to toggle theme.", error2);
                }
            }
        })();
    </script>
</c:if>
