import { z } from "zod";

export const commentSchema = z.object({
  text: z
    .string({ required_error: "Komentarz nie może być pusty." })
    .trim()
    .min(10, "Komentarz musi mieć co najmniej 10 znaków.")
    .max(2000, "Komentarz nie może być dłuższy niż 2000 znaków."),
});

export type CommentFormValues = z.infer<typeof commentSchema>;
