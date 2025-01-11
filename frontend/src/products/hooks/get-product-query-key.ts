const queryKey = ["products"] as const;

export const getProductQueryKey = (id?: string) =>
  id ? ([...queryKey, id] as const) : queryKey;
