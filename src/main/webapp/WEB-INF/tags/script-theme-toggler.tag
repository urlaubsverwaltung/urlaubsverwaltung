<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="uv" tagdir="/WEB-INF/tags" %>

<c:if test="${theme == 'system'}">
    <script>
        (function() {
            const htmlElement = document.querySelector("html");
            const mediaQueryDark = window.matchMedia('(prefers-color-scheme: dark)');
            if (mediaQueryDark.matches) {
                setDarkStuff();
            }
            try {
                mediaQueryDark.addEventListener("change", function () {
                    toggleStuff();
                });
            } catch (error) {
                // safari (https://stackoverflow.com/a/60000747)
                try {
                    mediaQueryDark.addListener(function() {
                        toggleStuff();
                    });
                } catch (error2) {
                    console.info("could not add mediaQuery listener to toggle theme.", error2);
                }
            }

            function toggleStuff() {
                if (htmlElement.classList.contains("tw-dark")) {
                    setLightStuff();
                } else {
                    setDarkStuff();
                }
            }
            function setDarkStuff() {
                htmlElement.classList.add("tw-dark");
                document.querySelector("meta[name='theme-color']").setAttribute("content", "#18181b");
            }
            function setLightStuff() {
                htmlElement.classList.remove("tw-dark");
                document.querySelector("meta[name='theme-color']").setAttribute("content", "#fafafa");
            }
        })();
    </script>
</c:if>
