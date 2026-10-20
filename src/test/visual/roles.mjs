// Demo persons of the 'demodata' profile, see DemoDataPersonCreationForLocalDevelopment and the keycloak realm import.
export const roles = [
  { name: "user", username: "user@focus-shift.de" },
  { name: "departmentHead", username: "departmentHead@focus-shift.de" },
  { name: "secondStageAuthority", username: "secondStageAuthority@focus-shift.de" },
  { name: "boss", username: "boss@focus-shift.de" },
  { name: "office", username: "office@focus-shift.de" },
  { name: "admin", username: "admin@focus-shift.de" },
];

export const password = "secret";

export function authFile(role) {
  return `target/visual-tests/auth/${role.name}.json`;
}
