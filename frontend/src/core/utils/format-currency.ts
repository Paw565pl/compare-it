export const formatCurrency = (value: number, currency: string) => {
  const intl = new Intl.NumberFormat("pl-PL", {
    style: "currency",
    currency,
  });

  const result = intl.format(value);
  const resultWithDot = result.replace(/,/g, ".");

  return resultWithDot;
};
