"use client";

import { H1 } from "@/core/components/ui/h1";
import { PriceAlertNotificationCard } from "@/price-alerts/components";
import { useFetchPriceAlertsPage } from "@/price-alerts/hooks/client/use-fetch-price-alerts-page";
import { useSession } from "next-auth/react";
import InfiniteScroll from "react-infinite-scroll-component";

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

  if (isError) return <span>Coś poszło nie tak!</span>;

  const isEmpty = priceAlertsPage?.pages.at(0)?.page.totalElements === 0;
  const fetchedPriceAlertsCount = priceAlertsPage?.pages.reduce(
    (acc, page) => acc + page.content.length,
    0,
  );

  return (
    <>
      <H1>Twoja historia alertów cenowych</H1>

      {/* {!isEmpty && (
        <DeleteConfirmationAlertDialog
          alertDialogTriggerLabel={"Wyczyść całą historię powiadomień"}
          alertDialogTriggerClassName="mb-4"
          handleDelete={() =>
            console.log("delete price all alert notifications")
          }
        />
      )} */}

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
        <section className="flex flex-wrap justify-center gap-2 sm:justify-start">
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
