// Copyright 2015-present 650 Industries. All rights reserved.

#import "EXScopedEventEmitter.h"

@class EXHomeModule;

@protocol EXHomeModuleDelegate <NSObject>

/**
 *  Whether the kernel JS should show any devtools UI or respond to devtools commands.
 */
- (BOOL)homeModuleShouldEnableDevtools:(EXHomeModule *)module;

/**
 *  Whether the kernel JS should auto reload an experience after it encounters a fatal JS error.
 */
- (BOOL)homeModuleShouldAutoReloadCurrentTask:(EXHomeModule *)module;

/**
 *  Whether to enable legacy gesture/button for the Expo menu.
 */
- (BOOL)homeModuleShouldEnableLegacyMenuBehavior:(EXHomeModule *)module;
- (void)homeModule:(EXHomeModule *)module didSelectEnableLegacyMenuBehavior:(BOOL)isEnabled;

/**
 *  Dictionary of `key` => `user facing label` items to show in the kernel JS dev menu.
 */
- (NSDictionary <NSString *, NSString *> *)devMenuItemsForHomeModule:(EXHomeModule *)module;

- (void)homeModule:(EXHomeModule *)module didSelectDevMenuItemWithKey:(NSString *)key;
- (void)homeModuleDidSelectHomeDiagnostics:(EXHomeModule *)module;
- (void)homeModule:(EXHomeModule *)module didOpenUrl:(NSString *)url;
- (void)homeModuleDidSelectRefresh:(EXHomeModule *)module;
- (void)homeModuleDidSelectCloseMenu:(EXHomeModule *)module;
- (void)homeModuleDidSelectGoToHome:(EXHomeModule *)module;

@end

@interface EXHomeModule : EXScopedEventEmitter

- (void)dispatchJSEvent: (NSString *)eventName
                   body: (NSDictionary *)eventBody
              onSuccess: (void (^)(NSDictionary *))success
              onFailure: (void (^)(NSString *))failure;

@end
