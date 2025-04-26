const defaultOptions: Intl.DateTimeFormatOptions = {
  dateStyle: "medium",
  timeStyle: "short",
} as const;

export const formatDate = (
  date: string,
  options?: Intl.DateTimeFormatOptions,
) =>
  new Intl.DateTimeFormat("pl-PL", options ?? defaultOptions).format(
    new Date(date),
  );
