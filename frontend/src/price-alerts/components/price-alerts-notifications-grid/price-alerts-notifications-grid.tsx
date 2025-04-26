"use client";

import { DeleteConfirmationAlertDialog } from "@/core/components";
import { Button } from "@/core/components/ui/button";
import { H1 } from "@/core/components/ui/h1";
import { PriceAlertNotificationCard } from "@/price-alerts/components";
import { useDeleteInactivePriceAlerts } from "@/price-alerts/hooks/client/use-delete-inactive-price-alerts";
import { useFetchPriceAlertsPage } from "@/price-alerts/hooks/client/use-fetch-price-alerts-page";
import { Trash2 } from "lucide-react";
import { useSession } from "next-auth/react";
import InfiniteScroll from "react-infinite-scroll-component";
import { toast } from "sonner";

export const PriceAlertsNotificationsGrid = () => {
  const { data: session } = useSession();

  const accessToken = session?.tokens?.accessToken as string;
  const userId = session?.user?.id as string;

  const {
    data: priceAlertsPage,
    hasNextPage,
    fetchNextPage,
    isError,
  } = useFetchPriceAlertsPage(accessToken, userId, { isActive: false });
  const { mutate: deleteInactivePriceAlerts } =
    useDeleteInactivePriceAlerts(accessToken);

  if (isError) return <span>Coś poszło nie tak!</span>;

  const isEmpty = priceAlertsPage?.pages.at(0)?.page.totalElements === 0;
  const fetchedPriceAlertsCount = priceAlertsPage?.pages.reduce(
    (acc, page) => acc + page.content.length,
    0,
  );

  const handleDeleteInactivePriceAlerts = () => {
    deleteInactivePriceAlerts(undefined, {
      onSuccess: () =>
        toast.success("Historia powiadomień cenowych została wyczyszczona."),
      onError: () => toast.error("Coś poszło nie tak!"),
    });
  };

  return (
    <>
      <H1>Twoja historia alertów cenowych</H1>

      {!isEmpty && (
        <DeleteConfirmationAlertDialog
          trigger={
            <Button variant="destructive" className="mb-4">
              <Trash2 />
              <span>Wyczyść całą historię powiadomień</span>
            </Button>
          }
          handleDelete={handleDeleteInactivePriceAlerts}
        />
      )}

      {isEmpty && (
        <div className="text-center sm:text-left">
          Lista powiadomień cenowych jest pusta.
        </div>
      )}

      <InfiniteScroll
        dataLength={fetchedPriceAlertsCount || 0}
        hasMore={hasNextPage}
        next={fetchNextPage}
        loader={null}
      >
        <section className="flex flex-wrap justify-center gap-3 sm:justify-start">
          {priceAlertsPage?.pages.map(({ content }) =>
            content?.map((priceAlert) => (
              <PriceAlertNotificationCard
                key={priceAlert.id}
                priceAlert={priceAlert}
              />
            )),
          )}
        </section>
      </InfiniteScroll>
    </>
  );
};
