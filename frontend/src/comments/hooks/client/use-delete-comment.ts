import { CommentEntity } from "@/comments/entities/comment-entity";
import { commentsQueryKey } from "@/comments/hooks/query-options/comments-query-key";
import { getQueryClient } from "@/core/libs/tanstack-query";
import { apiService } from "@/core/services/api";
import { ErrorResponse } from "@/core/services/api/types/error-response";
import { PaginatedData } from "@/core/services/api/types/paginated-data";
import { productsQueryKey } from "@/products/hooks/query-options/products-query-key";
import { InfiniteData, useMutation } from "@tanstack/react-query";
import { AxiosError } from "axios";
import { toast } from "sonner";

const deleteComment = async (
  accessToken: string,
  productId: string,
  commentId: string,
) => {
  const { data } = await apiService.delete<undefined>(
    `/api/v1/products/${productId}/comments/${commentId}`,
    {
      headers: {
        Authorization: `Bearer ${accessToken}`,
      },
    },
  );
  return data;
};

export const useDeleteComment = (
  accessToken: string,
  productId: string,
  commentId: string,
) => {
  const queryClient = getQueryClient();
  const commentPageQueryKey = [
    ...productsQueryKey,
    productId,
    ...commentsQueryKey,
  ] as const;

  return useMutation<
    undefined,
    AxiosError<ErrorResponse>,
    undefined,
    InfiniteData<PaginatedData<CommentEntity>>
  >({
    mutationKey: [
      ...productsQueryKey,
      productId,
      ...commentsQueryKey,
      commentId,
      "delete",
    ] as const,
    mutationFn: () => deleteComment(accessToken, productId, commentId),
    onMutate: async () => {
      await queryClient.cancelQueries({ queryKey: commentPageQueryKey });

      const previousCommentInfiniteData =
        queryClient.getQueryData<InfiniteData<PaginatedData<CommentEntity>>>(
          commentPageQueryKey,
        );

      const newCommentPages = previousCommentInfiniteData?.pages.map(
        ({ page, content }) => ({
          page,
          content: content.filter((comment) => comment.id !== commentId),
        }),
      );
      const newCommentInfiniteData = {
        pageParams: previousCommentInfiniteData?.pageParams,
        pages: newCommentPages,
      };
      queryClient.setQueryData(commentPageQueryKey, newCommentInfiniteData);

      return previousCommentInfiniteData;
    },
    onSuccess: () => {
      toast.success("Komentarz został usunięty.");
    },
    onError: (_, __, previousCommentPage) => {
      toast.error("Coś poszło nie tak!");
      queryClient.setQueryData(commentPageQueryKey, previousCommentPage);
    },
    onSettled: () => {
      queryClient.invalidateQueries({ queryKey: commentPageQueryKey });
    },
  });
};
