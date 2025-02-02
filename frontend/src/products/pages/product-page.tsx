import { prefetchCommentPage } from "@/comments/hooks/server/prefetch-comment-page";
import { getQueryClient } from "@/core/libs/tanstack-query";
import { ProductPageTop } from "@/products/components/index";
import { prefetchProduct } from "@/products/hooks/server/prefetch-product";
import { dehydrate, HydrationBoundary } from "@tanstack/react-query";

export interface ProductPageProps {
  readonly params: Promise<{ id: string }>;
}

export const ProductPage = async ({ params }: ProductPageProps) => {
  const productId = (await params).id;

  const queryClient = getQueryClient();

  await Promise.all([
    await prefetchProduct(queryClient, productId),
    await prefetchCommentPage(queryClient, productId),
  ]);

  return (
    <HydrationBoundary state={dehydrate(queryClient)}>
      <div className="flex flex-col">
        <ProductPageTop productId={productId} />
      </div>
    </HydrationBoundary>
  );
};
