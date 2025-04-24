import { auth } from "@/auth/config/auth-config";
import { prefetchCommentPage } from "@/comments/hooks/server/prefetch-comment-page";
import { getQueryClient } from "@/core/libs/tanstack-query";
import { prefetchFavoriteProductStatus } from "@/favorite-products/hooks/server/prefetch-favorite-product-status";
import { prefetchActivePriceAlertForProduct } from "@/price-alerts/hooks/server/prefetch-active-price-alert-for-product";
import { ProductPageTop } from "@/products/components/index";
import { prefetchProduct } from "@/products/hooks/server/prefetch-product";
import { dehydrate, HydrationBoundary } from "@tanstack/react-query";

export interface ProductPageProps {
  readonly params: Promise<{ id: string }>;
}

export const ProductPage = async ({ params }: ProductPageProps) => {
  const productId = (await params).id;

  const session = await auth();
  const accessToken = session?.tokens?.accessToken as string;
  const userId = session?.user?.id as string;

  const queryClient = getQueryClient();

  const queries = [prefetchProduct(queryClient, productId)];
  if (session)
    queries.push(
      prefetchFavoriteProductStatus(
        queryClient,
        accessToken,
        userId,
        productId,
      ),
      prefetchActivePriceAlertForProduct(
        queryClient,
        accessToken,
        userId,
        productId,
      ),
    );

  await Promise.all(queries);
  prefetchCommentPage(queryClient, productId, accessToken);

  return (
    <HydrationBoundary state={dehydrate(queryClient)}>
      <ProductPageTop productId={productId} />
    </HydrationBoundary>
  );
};
