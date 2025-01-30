import { auth } from "@/auth/config/auth-config";
import { getQueryClient } from "@/core/libs/tanstack-query";
import { prefetchFavoriteProductsPage } from "@/favorite-products/hooks/server/prefetch-favorite-products-page";
import { prefetchPriceAlertsPage } from "@/price-alerts/hooks/server/prefetch-price-alerts-page";
import { Profile } from "@/profile/components";
import { dehydrate, HydrationBoundary } from "@tanstack/react-query";

export const ProfilePage = async () => {
  const session = await auth();
  const accessToken = session?.tokens?.accessToken as string;
  const userId = session?.user?.id as string;

  const queryClient = getQueryClient();

  await Promise.all([
    prefetchFavoriteProductsPage(queryClient, accessToken, userId),
    prefetchPriceAlertsPage(queryClient, accessToken, userId, { active: true }),
    prefetchPriceAlertsPage(queryClient, accessToken, userId, {
      active: false,
    }),
  ]);

  return (
    <HydrationBoundary state={dehydrate(queryClient)}>
      <Profile />
    </HydrationBoundary>
  );
};
