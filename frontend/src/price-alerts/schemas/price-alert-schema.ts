import { z } from "zod";

export const priceAlertSchema = z.object({
  targetPrice: z
    .string({ error: "Cena docelowa jest wymagana." })
    .refine((value) => !isNaN(Number(value)), {
      message: "Cena docelowa musi być liczbą.",
    })
    .refine((value) => Number(value) > 0, {
      message: "Cena docelowa musi być liczbą dodatnią.",
    }),
  isOutletAllowed: z.boolean(),
});

export type PriceAlertFormValues = z.infer<typeof priceAlertSchema>;
