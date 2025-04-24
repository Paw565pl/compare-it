export const getIsObjectIdValid = (id: string) => /^[0-9a-fA-F]{24}$/.test(id);
