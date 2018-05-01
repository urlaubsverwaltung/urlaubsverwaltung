import { selectors as navSelectors } from './navigation'

export function doLogout(t) {
  return t.click(navSelectors.logout);
}

export default {
  doLogout,
}
