export function mutation(element) {
  return {
    attributeChanged(attributes) {
      return {
        subscribe(callback) {
          const observer = new MutationObserver(function (mutationRecords) {
            for (let mutation of mutationRecords) {
              const { type, attributeName, target, oldValue } = mutation;
              if (type === "attributes" && hasValueToggled(mutation)) {
                callback({
                  target,
                  data: {
                    attributeName: attributeName,
                    oldValue: oldValue,
                  },
                });
              }
            }
          });
          observer.observe(element, { attributeFilter: attributes, attributeOldValue: true });
        },
      };
    },
  };
}

function hasValueToggled(mutationRecord) {
  const { attributeName, oldValue, target } = mutationRecord;
  // eslint-disable-next-line unicorn/no-null
  return (oldValue == null && target.hasAttribute(attributeName)) || oldValue !== target.getAttribute(attributeName);
}
