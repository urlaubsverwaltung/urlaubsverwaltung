
export async function setup () {
    // jest is configured with 'jsdom' environment
    // so window is already available here
    // and we're able to attach additional custom stuff

    // importing jquery which is used in application
    await import('../../main/resources/static/lib/jquery/js/jquery-1.9.1');
    // trigger ready event
    // so "modules" registered via $(function() { /* ... */ })
    // are executed immediately on file import
    window.jQuery.ready();

    // defined in 'actions.js' as global function
    // setting as spy function to assert things in the tests
    window.tooltip = jest.fn();
}
